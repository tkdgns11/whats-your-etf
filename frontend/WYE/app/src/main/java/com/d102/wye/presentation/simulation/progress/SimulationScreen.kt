package com.d102.wye.presentation.simulation.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d102.wye.domain.state.InvestmentType
import com.d102.wye.presentation.designsystem.WyePortfolioDialog
import com.d102.wye.presentation.designsystem.WyeTabs
import com.d102.wye.presentation.designsystem.WyeTopBar
import com.d102.wye.presentation.model.UiState
import com.d102.wye.presentation.simulation.progress.result.InvestmentDictionaryDialog
import com.d102.wye.presentation.simulation.model.SimulationUiModel
import com.d102.wye.presentation.simulation.progress.result.AiReviewDialog
import com.d102.wye.presentation.simulation.progress.result.SimulationResultSection
import com.d102.wye.presentation.simulation.progress.setup.InvestmentSetupSection
import com.d102.wye.presentation.simulation.progress.setup.PortfolioSection
import com.d102.wye.presentation.theme.PrimaryGreen

@Composable
fun SimulationScreen(
    onBackClick: () -> Unit,
    onAddEtfClick: (currentTickers: List<String>) -> Unit,
    onSaveClick: () -> Unit,
    viewModel: SimulationViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val simulationState by viewModel.simulationState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val showAiDialog by viewModel.showAiDialog.collectAsStateWithLifecycle()
    val aiReviewState by viewModel.aiReviewState.collectAsStateWithLifecycle()
    val showSaveDialog by viewModel.showSaveDialog.collectAsStateWithLifecycle()
    val savePortfolioState by viewModel.savePortfolioState.collectAsStateWithLifecycle()

    val idleGuideMessage by viewModel.idleGuideMessage.collectAsStateWithLifecycle()

    var showDictionaryDialog by remember { mutableStateOf(false) }

    LaunchedEffect(simulationState) {
        if (simulationState is UiState.Error) {
            snackbarHostState.showSnackbar(
                message = (simulationState as UiState.Error).message
            )
        }
    }

    if (showAiDialog) {
        AiReviewDialog(
            uiState = aiReviewState,
            onDismiss = { viewModel.onAiDialogDismiss() }
        )
    }

    if (showDictionaryDialog) {
        InvestmentDictionaryDialog(onDismiss = { showDictionaryDialog = false })
    }

    if (showSaveDialog) {
        val defaultName = "포트폴리오 ${java.time.LocalDate.now()}"
        WyePortfolioDialog(
            title = "포트폴리오 저장하기",
            description = "나만의 투자 전략을 식별할 수 있는 이름을 지어주세요.\n저장한 포트폴리오는 나의 전략에서 확인할 수 있습니다.",
            initialName = "",
            placeholder = defaultName,
            confirmButtonText = if (savePortfolioState is UiState.Loading) "저장 중..." else "저장 완료",
            isLoading = savePortfolioState is UiState.Loading,
            errorMessage = (savePortfolioState as? UiState.Error)?.message,
            onDismiss = { viewModel.onSaveDialogDismiss() },
            onConfirm = { name ->
                viewModel.savePortfolio(name.ifBlank { defaultName })
                onSaveClick()
            }
        )
    }

    SimulationScreenContent(
        formState = formState,
        simulationState = simulationState,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        onTabSelected = { viewModel.onTabSelected(it) },
        onOverlayToggled = { viewModel.onOverlayToggled(it) },
        onInvestmentTypeSelected = { viewModel.onInvestmentTypeSelected(it) },
        onAmountChanged = { viewModel.onAmountChanged(it) },
        onPeriodChanged = { viewModel.onPeriodChanged(it) },
        onAddEtfClick = { onAddEtfClick(formState.portfolioItems.map { it.ticker }) },
        onPortfolioItemRemoved = { viewModel.onPortfolioItemRemoved(it) },
        onWeightChange = { ticker, weight -> viewModel.updateItemWeight(ticker, weight) },
        onAiDiagnosisClick = { viewModel.onAiReviewClick() },
        onDictionaryClick = { showDictionaryDialog = true },
        onSaveClick = { viewModel.onSaveIconClick() },
        idleGuideMessage = idleGuideMessage
    )
}

@Composable
private fun SimulationScreenContent(
    formState: SimulationFormState,
    simulationState: UiState<SimulationUiModel>,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onTabSelected: (Int) -> Unit,
    onOverlayToggled: (Boolean) -> Unit,
    onInvestmentTypeSelected: (InvestmentType) -> Unit,
    onAmountChanged: (String) -> Unit,
    onPeriodChanged: (String) -> Unit,
    onAddEtfClick: () -> Unit,
    onPortfolioItemRemoved: (String) -> Unit,
    onWeightChange: (String, Int) -> Unit,
    onAiDiagnosisClick: () -> Unit,
    onDictionaryClick: () -> Unit,
    onSaveClick: () -> Unit,
    idleGuideMessage: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        WyeTopBar(
            title = "투자 시뮬레이션",
            onBackClick = onBackClick,
            actions = {
                Text(
                    text = "저장",
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .background(PrimaryGreen, RoundedCornerShape(12.dp))
                        .clickable { onSaveClick() }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        )

        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                WyeTabs(
                    titles = listOf("수익률 추이", "포트폴리오 분석"),
                    selectedIndex = formState.selectedTabIndex,
                    onTabSelected = onTabSelected
                )

                // 결과 섹션 (차트 + 지표)
                SimulationResultSection(
                    formState = formState,
                    simulationState = simulationState,
                    onOverlayToggled = onOverlayToggled,
                    onAiDiagnosisClick = onAiDiagnosisClick,
                    onDictionaryClick = onDictionaryClick,
                    idleGuideMessage = idleGuideMessage
                )

                // 설정 섹션
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White)
                        .verticalScroll(rememberScrollState())
                ) {
                    // 투자 설정
                    InvestmentSetupSection(
                        formState = formState,
                        onInvestmentTypeSelected = onInvestmentTypeSelected,
                        onAmountChanged = onAmountChanged,
                        onPeriodChanged = onPeriodChanged
                    )

                    // 포트폴리오 구성
                    PortfolioSection(
                        formState = formState,
                        onAddClick = onAddEtfClick,
                        onRemoveClick = onPortfolioItemRemoved,
                        onWeightChange = onWeightChange
                    )
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}