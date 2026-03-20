package com.d102.wye.presentation.simulation.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d102.wye.presentation.designsystem.WyePortfolioDialog
import com.d102.wye.presentation.designsystem.WyeTabs
import com.d102.wye.presentation.designsystem.WyeTopBar
import com.d102.wye.presentation.model.UiState
import com.d102.wye.presentation.simulation.progress.result.AiReviewDialog
import com.d102.wye.presentation.simulation.progress.result.InvestmentDictionaryDialog
import com.d102.wye.presentation.simulation.progress.result.SimulationResultSection
import com.d102.wye.presentation.simulation.progress.setup.InvestmentSetupSection
import com.d102.wye.presentation.simulation.progress.setup.PortfolioSection
import com.d102.wye.presentation.theme.BackGroundLightGreen2
import com.d102.wye.presentation.theme.PrimaryGreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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

    // 화면 높이의 45%를 peekHeight로
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val peekHeight = screenHeight * 0.33f

    val scaffoldState = rememberBottomSheetScaffoldState()

    val coroutineScope = rememberCoroutineScope()

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

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = peekHeight,
        sheetContainerColor = BackGroundLightGreen2,
        sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = BackGroundLightGreen2,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                InvestmentSetupSection(
                    formState = formState,
                    onInvestmentTypeSelected = { viewModel.onInvestmentTypeSelected(it) },
                    onAmountChanged = { viewModel.onAmountChanged(it) },
                    onPeriodChanged = { viewModel.onPeriodChanged(it) }
                )
                PortfolioSection(
                    formState = formState,
                    onAddClick = { onAddEtfClick(formState.portfolioItems.map { it.ticker }) },
                    onRemoveClick = { viewModel.onPortfolioItemRemoved(it) },
                    onWeightChange = { ticker, weight ->
                        viewModel.updateItemWeight(
                            ticker,
                            weight
                        )
                    },
                    onConfirmClick = {
                        coroutineScope.launch {
                            scaffoldState.bottomSheetState.partialExpand()
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                            .clickable { viewModel.onSaveIconClick() }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            )

            WyeTabs(
                titles = listOf("수익률 추이", "포트폴리오 분석"),
                selectedIndex = formState.selectedTabIndex,
                onTabSelected = { viewModel.onTabSelected(it) }
            )

            Box(modifier = Modifier.fillMaxSize()) {
                SimulationResultSection(
                    formState = formState,
                    simulationState = simulationState,
                    onOverlayToggled = { viewModel.onOverlayToggled(it) },
                    onAiDiagnosisClick = { viewModel.onAiReviewClick() },
                    onDictionaryClick = { showDictionaryDialog = true },
                    idleGuideMessage = idleGuideMessage
                )
            }
        }
    }
}