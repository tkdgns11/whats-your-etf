package com.d102.wye.presentation.simulation.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d102.wye.domain.state.InvestmentType
import com.d102.wye.presentation.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SimulationSetupViewModel @Inject constructor(
    // TODO: Repository 주입
    // private val simulationRepository: SimulationRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(SimulationFormState())
    val formState: StateFlow<SimulationFormState> = _formState.asStateFlow()

    private val _resultState = MutableStateFlow<UiState<SimulationResult>>(UiState.Idle)
    val resultState: StateFlow<UiState<SimulationResult>> = _resultState.asStateFlow()


    fun onTabSelected(index: Int) {
        _formState.update { it.copy(selectedTabIndex = index) }
    }

    fun onOverlayToggled(enabled: Boolean) {
        _formState.update { it.copy(isOverlayEnabled = enabled) }
    }

    fun onInvestmentTypeSelected(type: InvestmentType) {
        _formState.update { it.copy(investmentType = type) }
        calculateSimulation()
    }

    fun onAmountChanged(amount: String) {
        _formState.update { it.copy(investmentAmount = amount) }
        calculateSimulation()
    }

    fun onPeriodChanged(period: String) {
        _formState.update { it.copy(investmentPeriod = period) }
        calculateSimulation()
    }

    fun onPortfolioItemAdded(item: PortfolioItem) {
        _formState.update { it.copy(portfolioItems = it.portfolioItems + item) }
        calculateSimulation()
    }

    fun onPortfolioItemRemoved(ticker: String) {
        _formState.update {
            it.copy(portfolioItems = it.portfolioItems.filter { item -> item.ticker != ticker })
        }
        calculateSimulation()
    }

    // ── 계산 ────────────────────────────────

    private fun calculateSimulation() {
        val form = _formState.value

        // 필수 입력값 미충족 시 결과 초기화
        if (form.investmentAmount.isBlank() ||
            form.investmentPeriod.isBlank() ||
            form.portfolioItems.isEmpty()
        ) {
            _resultState.update { UiState.Idle }
            return
        }

        viewModelScope.launch {
            _resultState.update { UiState.Loading }

            // TODO: 로컬 계산 로직
            // when (val result = simulationRepository.calculate(
            //     type = form.investmentType,
            //     amount = form.investmentAmount.toLong(),
            //     period = form.investmentPeriod.toInt(),
            //     portfolio = form.portfolioItems
            // )) {
            //     is BaseResult.Success -> _resultState.update { UiState.Success(result.data.toSimulationResult()) }
            //     is BaseResult.Error   -> _resultState.update { UiState.Error(result.error.message) }
            // }
        }
    }
}

// ─────────────────────────────────────────
// 화면 데이터 모델
// ─────────────────────────────────────────

data class PortfolioItem(
    val ticker: String,
    val name: String,
    val weight: Int                         // 비중 (%)
)

// 폼 입력 상태 (사용자가 직접 조작하는 값들)
data class SimulationFormState(
    val selectedTabIndex: Int = 0,          // 0: 수익률 추이, 1: 포트폴리오 분석
    val isOverlayEnabled: Boolean = false,  // 내 보유 자산 겹쳐보기
    val investmentType: InvestmentType = InvestmentType.INSTALLMENT,
    val investmentAmount: String = "",
    val investmentPeriod: String = "",
    val portfolioItems: List<PortfolioItem> = emptyList()
)

// 시뮬레이션 계산 결과 (서버 or 로컬 계산 후 나오는 값)
data class SimulationResult(
    val estimatedFinalAsset: String,
    val yieldRate: String,
    val totalInvestment: String
)