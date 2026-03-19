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
import com.d102.wye.presentation.simulation.model.SimulationUiModel
import com.d102.wye.presentation.simulation.analysis.PortfolioAnalysisView
import com.d102.wye.presentation.simulation.progress.SimulationFormState
import com.d102.wye.presentation.simulation.progress.components.AiReviewButton

@Composable
fun SimulationResultSection(
    formState: SimulationFormState,
    simulationState: UiState<SimulationUiModel>,
    onOverlayToggled: (Boolean) -> Unit,
    onAiDiagnosisClick: () -> Unit,
    onDictionaryClick: () -> Unit,
    idleGuideMessage: String
) {
    Box {
        SubcomposeLayout(
            modifier = Modifier
                .bottomShadow(offsetY = 4.dp, blurRadius = 6.dp)
                .background(Color.White)
                .padding(16.dp)
        ) { constraints ->

            val yieldHeight = subcompose("yield_measure") {
                YieldTrendView(formState, simulationState, onOverlayToggled, idleGuideMessage)
            }.map { it.measure(constraints) }.maxOfOrNull { it.height } ?: 0

            val analysisHeight = subcompose("analysis_measure") {
                PortfolioAnalysisView(formState, simulationState, onDictionaryClick)
            }.map { it.measure(constraints) }.maxOfOrNull { it.height } ?: 0

            val maxHeight = maxOf(yieldHeight, analysisHeight)

            val fixedConstraints = constraints.copy(
                minHeight = maxHeight,
                maxHeight = maxHeight
            )

            val content = subcompose("content") {
                when (formState.selectedTabIndex) {
                    0 -> YieldTrendView(formState, simulationState, onOverlayToggled, idleGuideMessage)
                    1 -> PortfolioAnalysisView(formState, simulationState, onDictionaryClick)
                }
            }.map { it.measure(fixedConstraints) }

            layout(constraints.maxWidth, maxHeight) {
                content.forEach { it.place(0, 0) }
            }
        }

        AiReviewButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp),
            isEmpty = simulationState !is UiState.Success,
            onClick = onAiDiagnosisClick
        )
    }
}