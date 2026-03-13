package com.d102.wye.presentation.simulation.progress.result

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.presentation.designsystem.DashedContainer
import com.d102.wye.presentation.designsystem.WyeCard
import com.d102.wye.presentation.model.UiState
import com.d102.wye.presentation.simulation.model.SimulationUiModel
import com.d102.wye.presentation.simulation.progress.SimulationFormState
import com.d102.wye.presentation.simulation.progress.components.ResultCard
import com.d102.wye.presentation.theme.IconInactive
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.SurfaceCard
import com.d102.wye.presentation.theme.SurfaceDivider
import com.d102.wye.presentation.theme.SurfaceVariant
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary

@Composable
fun YieldTrendView(
    formState: SimulationFormState,
    simulationState: UiState<SimulationUiModel>,
    onOverlayToggled: (Boolean) -> Unit,
    idleGuideMessage: String
) {
    Column {
        // ── 내 보유 자산 겹쳐보기 토글 ──────────────────────────────────────
        WyeCard(
            modifier = Modifier.fillMaxWidth(),
            innerPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            border = BorderStroke(1.dp, SurfaceVariant),
            containerColor = SurfaceCard,
            elevation = 0.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "내 보유 자산 겹쳐보기",
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                    )
                    Text(
                        text = "현재 자산과 시뮬레이션 비교",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
                Switch(
                    checked = formState.isOverlayEnabled,
                    onCheckedChange = onOverlayToggled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = PrimaryGreen,
                        checkedBorderColor = PrimaryGreen,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = SurfaceDivider,
                        uncheckedBorderColor = SurfaceDivider,
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── 요약 카드 3개 ────────────────────────────────────────────────────
        val uiModel = (simulationState as? UiState.Success)?.data
        val cardItems = listOf(
            "예상 수익금" to (uiModel?.estimatedFinalAsset ?: "-"),
            "수익률"     to (uiModel?.yieldRate ?: "-"),
            "총 투자금"  to (uiModel?.totalInvestment ?: "-")
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            cardItems.forEach { (title, value) ->
                ResultCard(
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp),
                    borderColor = PrimaryGreen.copy(alpha = 0.1f),
                    backgroundColor = PrimaryGreen.copy(alpha = 0.05f)
                ) {
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = value,
                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── 차트 영역 ────────────────────────────────────────────────────────
        when (simulationState) {

            is UiState.Idle -> {
                // 플레이스홀더
                DashedContainer(height = 180.dp, strokeWidth = 2.dp) {
                    Text(
                        text = idleGuideMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = IconInactive,
                        textAlign = TextAlign.Center
                    )
                }
            }

            is UiState.Loading -> {
                // 비중 합계가 100%가 아닐 때 안내 문구
                val totalWeight = formState.portfolioItems.sumOf { it.weight }
                DashedContainer(height = 180.dp, strokeWidth = 2.dp) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (totalWeight != 100) {
                            Text(
                                text = "비중 합계: $totalWeight%",
                                style = MaterialTheme.typography.titleSmall,
                                color = if (totalWeight > 100) Color(0xFFE53935) else TextPrimary
                            )
                            Text(
                                text = if (totalWeight > 100) "비중 합계가 100%를 초과했습니다"
                                else "비중 합계를 100%로 맞춰주세요",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            CircularProgressIndicator(
                                color = PrimaryGreen,
                                strokeWidth = 3.dp
                            )
                            Text(
                                text = "계산 중...",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            is UiState.Success -> {
                // 백테스트 차트
                BacktestChart(
                    points = simulationState.data.backtestPoints,
                    investmentType = simulationState.data.investmentType,
                    isPositive = simulationState.data.isPositiveReturn,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            is UiState.Error -> {
                DashedContainer(height = 180.dp, strokeWidth = 2.dp) {
                    Text(
                        text = simulationState.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE53935),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}