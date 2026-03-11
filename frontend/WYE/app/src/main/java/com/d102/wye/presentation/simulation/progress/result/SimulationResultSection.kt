package com.d102.wye.presentation.simulation.progress.result

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.dp
import com.d102.wye.presentation.designsystem.bottomShadow
import com.d102.wye.presentation.model.UiState
import com.d102.wye.presentation.simulation.analysis.PortfolioAnalysisView
import com.d102.wye.presentation.simulation.progress.SimulationFormState
import com.d102.wye.presentation.simulation.progress.SimulationResult
import com.d102.wye.presentation.simulation.progress.components.AiDiagnosisButton

@Composable
fun SimulationResultSection(
    formState: SimulationFormState,
    resultState: UiState<SimulationResult>,
    onOverlayToggled: (Boolean) -> Unit,
    onAiDiagnosisClick: () -> Unit,
    onDictionaryClick: () -> Unit
) {
    Box {
        // 두 탭을 모두 측정해서 큰 높이로 고정
        SubcomposeLayout(
            modifier = Modifier
                .bottomShadow(offsetY = 4.dp, blurRadius = 6.dp)
                .background(Color.White)
                .padding(16.dp)
        ) { constraints ->

            // 두 탭 높이 측정
            val yieldHeight = subcompose("yield_measure") {
                YieldTrendView(formState, resultState, onOverlayToggled)
            }.map { it.measure(constraints) }.maxOfOrNull { it.height } ?: 0

            val analysisHeight = subcompose("analysis_measure") {
                PortfolioAnalysisView(formState, resultState, onDictionaryClick)
            }.map { it.measure(constraints) }.maxOfOrNull { it.height } ?: 0

            val maxHeight = maxOf(yieldHeight, analysisHeight)

            // 실제 표시할 탭만 렌더링
            val fixedConstraints = constraints.copy(
                minHeight = maxHeight,
                maxHeight = maxHeight
            )

            val content = subcompose("content") {
                when (formState.selectedTabIndex) {
                    0 -> YieldTrendView(formState, resultState, onOverlayToggled)
                    1 -> PortfolioAnalysisView(formState, resultState, onDictionaryClick)
                }
            }.map { it.measure(fixedConstraints) }

            layout(constraints.maxWidth, maxHeight) {
                content.forEach { it.place(0, 0) }
            }
        }

        // 버튼
        AiDiagnosisButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            isEmpty = resultState !is UiState.Success,
            onClick = onAiDiagnosisClick
        )
    }
}