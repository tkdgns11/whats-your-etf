package com.d102.wye.presentation.simulation.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d102.wye.domain.state.InvestmentType
import com.d102.wye.presentation.designsystem.WyeTopBar
import com.d102.wye.presentation.model.UiState
import com.d102.wye.presentation.simulation.progress.result.AiDiagnosisDialog
import com.d102.wye.presentation.simulation.progress.result.InvestmentDictionaryDialog
import com.d102.wye.presentation.simulation.progress.result.SimulationResultSection
import com.d102.wye.presentation.simulation.progress.setup.InvestmentSetupSection
import com.d102.wye.presentation.simulation.progress.setup.PortfolioSection
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.TextSecondary

@Composable
fun SimulationScreen(
    onBackClick: () -> Unit,
    onAddEtfClick: () -> Unit,          // ETF 종목 추가 화면으로 이동
    viewModel: SimulationSetupViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val resultState by viewModel.resultState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val showAiDialog by viewModel.showAiDialog.collectAsStateWithLifecycle()
    val aiDiagnosisState by viewModel.aiDiagnosisState.collectAsStateWithLifecycle()

    var showDictionaryDialog by remember { mutableStateOf(false) }

    LaunchedEffect(resultState) {
        if (resultState is UiState.Error) {
            snackbarHostState.showSnackbar(
                message = (resultState as UiState.Error).message
            )
        }
    }

    if (showAiDialog) {
        AiDiagnosisDialog(
            uiState = aiDiagnosisState,
            onDismiss = { viewModel.onAiDialogDismiss() }
        )
    }

    if (showDictionaryDialog) {
        InvestmentDictionaryDialog(
            onDismiss = { showDictionaryDialog = false }
        )
    }

    SimulationSetupScreenContent(
        formState = formState,
        resultState = resultState,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        onTabSelected = { viewModel.onTabSelected(it) },
        onOverlayToggled = { viewModel.onOverlayToggled(it) },
        onInvestmentTypeSelected = { viewModel.onInvestmentTypeSelected(it) },
        onAmountChanged = { viewModel.onAmountChanged(it) },
        onPeriodChanged = { viewModel.onPeriodChanged(it) },
        onAddEtfClick = onAddEtfClick,
        onPortfolioItemRemoved = { viewModel.onPortfolioItemRemoved(it) },
        onAiDiagnosisClick = { viewModel.onAiDiagnosisClick() },
        onDictionaryClick = { showDictionaryDialog = true }
    )
}

@Composable
private fun SimulationSetupScreenContent(
    formState: SimulationFormState,
    resultState: UiState<SimulationResult>,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onTabSelected: (Int) -> Unit,
    onOverlayToggled: (Boolean) -> Unit,
    onInvestmentTypeSelected: (InvestmentType) -> Unit,
    onAmountChanged: (String) -> Unit,
    onPeriodChanged: (String) -> Unit,
    onAddEtfClick: () -> Unit,
    onPortfolioItemRemoved: (String) -> Unit,
    onAiDiagnosisClick: () -> Unit,
    onDictionaryClick: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.background(Color.White),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
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
                            .clickable { /* TODO */ }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            SimulationTabs(
                selectedIndex = formState.selectedTabIndex,
                onTabSelected = onTabSelected
            )

            // 1. 결과 및 차트
            SimulationResultSection(
                formState = formState,
                resultState = resultState,
                onOverlayToggled = onOverlayToggled,
                onAiDiagnosisClick = onAiDiagnosisClick,
                onDictionaryClick = onDictionaryClick
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
            ) {
                // 2. 투자 설정
                InvestmentSetupSection(
                    formState = formState,
                    onInvestmentTypeSelected = onInvestmentTypeSelected,
                    onAmountChanged = onAmountChanged,
                    onPeriodChanged = onPeriodChanged
                )

                // 3. 포트폴리오 구성
                PortfolioSection(
                    formState = formState,
                    onAddClick = onAddEtfClick,
                    onRemoveClick = onPortfolioItemRemoved
                )
            }
        }
    }
}

@Composable
private fun SimulationTabs(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf("수익률 추이", "포트폴리오 분석")
    TabRow(
        selectedTabIndex = selectedIndex,
        containerColor = Color.White,
        contentColor = PrimaryGreen,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                color = PrimaryGreen
            )
        }
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedIndex == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                        color = if (selectedIndex == index) PrimaryGreen else TextSecondary,
                    )
                }
            )
        }
    }
}