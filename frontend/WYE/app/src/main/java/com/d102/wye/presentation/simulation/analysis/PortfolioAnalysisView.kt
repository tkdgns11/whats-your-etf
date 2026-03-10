package com.d102.wye.presentation.simulation.analysis

import DonutChart
import SectorWeight
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.presentation.designsystem.WyeCard
import com.d102.wye.presentation.model.UiState
import com.d102.wye.presentation.simulation.progress.SimulationFormState
import com.d102.wye.presentation.simulation.progress.SimulationResult
import com.d102.wye.presentation.simulation.progress.components.ResultCard
import com.d102.wye.presentation.theme.BackGroundLightGreen3
import com.d102.wye.presentation.theme.Divider
import com.d102.wye.presentation.theme.SurfaceCard
import com.d102.wye.presentation.theme.SurfaceVariant
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary

@Composable
fun PortfolioAnalysisView(
    formState: SimulationFormState,
    resultState: UiState<SimulationResult>,
    onDictionaryClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "포트폴리오 주요 지표",
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                color = TextSecondary,
            )

            Text(
                text = "ⓘ 용어 가이드",
                textAlign = TextAlign.Center,
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .border(1.dp, Divider, RoundedCornerShape(12.dp))
                    .clickable { onDictionaryClick() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        val resultData = (resultState as? UiState.Success)?.data
        val cardItems = listOf(
            "PER" to (resultData?.per ?: "24.5배"),
            "PBR" to (resultData?.pbr ?: "3.2배"),
            "ROE" to (resultData?.roe ?: "15%")
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            cardItems.forEach { (title, value) ->
                ResultCard(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp),
                    borderColor = SurfaceVariant,
                    backgroundColor = BackGroundLightGreen3
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = value,
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

//        val sectorData = remember { emptyList<SectorWeight>() }
        val sectorData = remember {
            listOf(
                SectorWeight("IT", 45f, Color(0xFF4B6B4E)),    // PrimaryGreen 계열
                SectorWeight("금융", 25f, Color(0xFF7E977A)),  // 연한 녹색
                SectorWeight("에너지", 15f, Color(0xFFA6BC9F)), // 더 연한 녹색
                SectorWeight("기타", 15f, Color(0xFFD1D5DB))   // 회색
            )
        }

        val isEmpty = sectorData.isEmpty()

        WyeCard(
            modifier = Modifier.fillMaxWidth(),
            innerPadding = PaddingValues(20.dp),
            border = BorderStroke(1.dp, SurfaceVariant),
            containerColor = SurfaceCard,
            elevation = 0.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 1. 타이틀
                Text(
                    text = "섹터 비중",
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                    color = TextSecondary,
                    modifier = Modifier.fillMaxWidth(),
                )

                // 2. 도넛 차트 (이제 오버레이 로직이 내부에 포함됨)
                DonutChart(items = sectorData)

                // 3. 범례 영역 (높이 고정)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 24.dp), // 범례 영역의 최소 높이 확보
                    contentAlignment = Alignment.Center
                ) {
                    if (!isEmpty) {
                        // 데이터가 있을 때만 범례를 그림
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            sectorData.forEach { sector ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(sector.color, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${sector.name} ${sector.ratio.toInt()}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}