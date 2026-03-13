package com.d102.wye.presentation.simulation.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.AiDiagnosisResult
import com.d102.wye.domain.repository.SimulationRepository
import com.d102.wye.domain.state.InvestmentType
import com.d102.wye.domain.usecase.simulation.RunSimulationUseCase
import com.d102.wye.presentation.model.UiState
import com.d102.wye.presentation.simulation.model.SimulationUiModel
import com.d102.wye.presentation.simulation.model.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SimulationViewModel @Inject constructor(
    private val runSimulation: RunSimulationUseCase,
    private val simulationRepository: SimulationRepository
) : ViewModel() {

    // 투자 설정 상태
    private val _formState = MutableStateFlow(SimulationFormState())
    val formState: StateFlow<SimulationFormState> = _formState.asStateFlow()

    // 시뮬레이션 결과 상태
    private val _simulationState = MutableStateFlow<UiState<SimulationUiModel>>(UiState.Idle)
    val simulationState: StateFlow<UiState<SimulationUiModel>> = _simulationState.asStateFlow()

    // AI 진단 다이얼로그 표시 여부
    private val _showAiDialog = MutableStateFlow(false)
    val showAiDialog: StateFlow<Boolean> = _showAiDialog.asStateFlow()

    // AI 진단 API 통신 상태
    private val _aiDiagnosisState = MutableStateFlow<UiState<AiDiagnosisResult>>(UiState.Idle)
    val aiDiagnosisState: StateFlow<UiState<AiDiagnosisResult>> = _aiDiagnosisState.asStateFlow()

    // 포트폴리오 저장 다이얼로그 표시 여부
    private val _showSaveDialog = MutableStateFlow(false)
    val showSaveDialog: StateFlow<Boolean> = _showSaveDialog.asStateFlow()

    // 포트폴리오 저장 API 통신 상태
    private val _savePortfolioState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val savePortfolioState: StateFlow<UiState<Unit>> = _savePortfolioState.asStateFlow()

    private var calcJob: Job? = null

    // ─────────────────────────────────────────────────────────────────────────
    // 포트폴리오 CRUD
    // ─────────────────────────────────────────────────────────────────────────

    fun addPortfolioItems(tickers: List<String>) {
        _formState.update { current ->
            val items = current.portfolioItems.toMutableList()
            tickers.forEach { ticker ->
                // 이미 담긴 종목은 무시
                if (items.none { it.ticker == ticker }) {
                    // TODO: ticker → name API 조회로 교체
                    items.add(PortfolioItem(ticker = ticker, name = ticker, weight = 0))
                }
            }
            current.copy(portfolioItems = items)
        }
        triggerCalculation()
    }

    fun onPortfolioItemRemoved(ticker: String) {
        _formState.update { current ->
            current.copy(
                portfolioItems = current.portfolioItems.filter { it.ticker != ticker }
            )
        }
        triggerCalculation()
    }

    fun updateItemWeight(ticker: String, newWeight: Int) {
        _formState.update { current ->
            current.copy(
                portfolioItems = current.portfolioItems.map { item ->
                    if (item.ticker == ticker) item.copy(weight = newWeight.coerceIn(0, 100))
                    else item
                }
            )
        }
        triggerCalculation()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UI 이벤트
    // ─────────────────────────────────────────────────────────────────────────

    val idleGuideMessage: StateFlow<String> = _formState.map { form ->
        when {
            form.investmentAmount.isBlank() || form.investmentPeriod.isBlank() -> "투자 금액과 기간을 입력하면\n수익률 그래프가 나타납니다."
            else -> "ETF를 추가하고 자산의 미래를 확인해보세요"
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "분석할 ETF를 먼저 추가해주세요."
    )

    fun onTabSelected(index: Int) =
        _formState.update { it.copy(selectedTabIndex = index) }

    fun onOverlayToggled(enabled: Boolean) =
        _formState.update { it.copy(isOverlayEnabled = enabled) }

    fun onInvestmentTypeSelected(type: InvestmentType) {
        _formState.update { it.copy(investmentType = type) }
        triggerCalculation()
    }

    fun onAmountChanged(amount: String) {
        _formState.update { it.copy(investmentAmount = amount) }
        triggerCalculation()
    }

    fun onPeriodChanged(period: String) {
        _formState.update { it.copy(investmentPeriod = period) }
        triggerCalculation()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // AI 진단
    // ─────────────────────────────────────────────────────────────────────────

    fun onAiDiagnosisClick() {
        _showAiDialog.value = true
        fetchAiDiagnosis()
    }

    fun onAiDialogDismiss() {
        _showAiDialog.value = false
    }

    private fun fetchAiDiagnosis() {
        if (_aiDiagnosisState.value is UiState.Loading ||
            _aiDiagnosisState.value is UiState.Success
        ) return

        viewModelScope.launch {
            _aiDiagnosisState.update { UiState.Loading }
            delay(1500) // TODO: 실제 AI 진단 API 호출
            _aiDiagnosisState.update {
                UiState.Success(
                    AiDiagnosisResult(
                        mainTitle = "공격적인 수익 추구!",
                        subTitle = "기술주 중심의 로켓 포트폴리오 🚀",
                        tags = listOf("기술주집중", "고변동성", "성장중심"),
                        feedback = "현재 포트폴리오는 특정 섹터에 집중되어 있어 변동성이 매우 큽니다. " +
                                "장기적인 안정을 위해 배당형 ETF로의 분산투자를 고려해보세요."
                    )
                )
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 저장
    // ─────────────────────────────────────────────────────────────────────────

    fun onSaveIconClick() {
        _showSaveDialog.value = true
    }

    fun onSaveDialogDismiss() {
        _showSaveDialog.value = false
        _savePortfolioState.value = UiState.Idle
    }

    fun savePortfolio(portfolioName: String) {
        if (_savePortfolioState.value is UiState.Loading) return
        viewModelScope.launch {
            _savePortfolioState.update { UiState.Loading }
            delay(1000) // TODO: 실제 저장 API 호출
            _savePortfolioState.update { UiState.Success(Unit) }
            _showSaveDialog.value = false
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 계산 트리거 (300ms debounce)
    // ─────────────────────────────────────────────────────────────────────────

    private fun triggerCalculation() {
        calcJob?.cancel()
        calcJob = viewModelScope.launch {
            delay(300)

            val form = _formState.value
            val totalWeight = form.portfolioItems.sumOf { it.weight }
            val amount = form.investmentAmount.toLongOrNull() ?: 0L
            val periodMonths = form.investmentPeriod.toIntOrNull() ?: 0

            // 입력 미완성
            if (form.portfolioItems.isEmpty() || amount <= 0L || periodMonths <= 0) {
                _simulationState.update { UiState.Idle }
                return@launch
            }

            // 비중 합계 100% 미달 → Loading (차트 영역에 안내 표시)
            if (totalWeight != 100) {
                _simulationState.update { UiState.Loading }
                return@launch
            }

            _simulationState.update { UiState.Loading }

            when (val result = runSimulation(
                RunSimulationUseCase.Params(
                    portfolios = form.portfolioItems.toDomain(),
                    investmentAmount = amount,
                    investmentType = form.investmentType,
                    periodMonths = periodMonths
                )
            )) {
                is BaseResult.Success -> _simulationState.update {
                    UiState.Success(result.data.toUiModel(form.investmentType))
                }

                is BaseResult.Error -> _simulationState.update {
                    UiState.Error(result.error.message)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Presentation 전용 모델
// ─────────────────────────────────────────────────────────────────────────────

data class SimulationFormState(
    val selectedTabIndex: Int = 0,
    val isOverlayEnabled: Boolean = false,
    val investmentType: InvestmentType = InvestmentType.INSTALLMENT,
    val investmentAmount: String = "",
    val investmentPeriod: String = "",
    val portfolioItems: List<PortfolioItem> = emptyList()
)
