package com.d102.wye.presentation.simulation.progress.result

import SectorWeight
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.R
import com.d102.wye.presentation.designsystem.DashedContainer
import com.d102.wye.presentation.designsystem.WyeCard
import com.d102.wye.presentation.model.UiState
import com.d102.wye.presentation.simulation.progress.SimulationFormState
import com.d102.wye.presentation.simulation.progress.SimulationResult
import com.d102.wye.presentation.theme.Divider
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
    resultState: UiState<SimulationResult>,
    onOverlayToggled: (Boolean) -> Unit
) {
    Column {
        WyeCard(
            modifier = Modifier.fillMaxWidth(),
            innerPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            border = BorderStroke(1.dp, SurfaceVariant),
            containerColor = SurfaceCard,
            elevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
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

        // 결과 카드 3개
        val resultData = (resultState as? UiState.Success)?.data
        val cardItems = listOf(
            "예상 수익금" to (resultData?.estimatedFinalAsset ?: "-"),
            "수익률" to (resultData?.yieldRate ?: "-"),
            "총 투자금" to (resultData?.totalInvestment ?: "-")
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

        DashedContainer(
            strokeWidth = 2.dp
        ) {
            Text(
                text = "ETF를 추가하고 자산의 미래를 확인해보세요",
                style = MaterialTheme.typography.bodyMedium,
                color = IconInactive
            )
        }
    }
}
