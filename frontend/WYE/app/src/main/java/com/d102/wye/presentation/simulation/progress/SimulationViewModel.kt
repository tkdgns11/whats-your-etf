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
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class SimulationViewModel @Inject constructor(
    private val runSimulation: RunSimulationUseCase,
    private val simulationRepository: SimulationRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(SimulationFormState())
    val formState: StateFlow<SimulationFormState> = _formState.asStateFlow()

    private val _simulationState = MutableStateFlow<UiState<SimulationUiModel>>(UiState.Idle)
    val simulationState: StateFlow<UiState<SimulationUiModel>> = _simulationState.asStateFlow()

    private val _showAiDialog = MutableStateFlow(false)
    val showAiDialog: StateFlow<Boolean> = _showAiDialog.asStateFlow()

    private val _aiDiagnosisState = MutableStateFlow<UiState<AiDiagnosisResult>>(UiState.Idle)
    val aiDiagnosisState: StateFlow<UiState<AiDiagnosisResult>> = _aiDiagnosisState.asStateFlow()

    private val _showSaveDialog = MutableStateFlow(false)
    val showSaveDialog: StateFlow<Boolean> = _showSaveDialog.asStateFlow()

    private val _savePortfolioState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val savePortfolioState: StateFlow<UiState<Unit>> = _savePortfolioState.asStateFlow()

    private var calcJob: Job? = null

    // ─────────────────────────────────────────────────────────────────────────
    // 포트폴리오 CRUD
    // ─────────────────────────────────────────────────────────────────────────

    fun addPortfolioItems(tickers: List<String>) {
        Timber.d("[Portfolio] addPortfolioItems 호출 | tickers=$tickers")

        viewModelScope.launch {
            val currentTickers = _formState.value.portfolioItems.map { it.ticker }
            val newTickers = tickers.filter { it !in currentTickers }
            Timber.d("[Portfolio] 현재 포트폴리오=$currentTickers | 신규 ticker=$newTickers")

            if (newTickers.isNotEmpty()) {
                _simulationState.update { UiState.Loading }

                val newToBeFetched = newTickers.filter {
                    val hasCached = simulationRepository.hasCachedPriceHistory(it)
                    Timber.d("[Cache] ticker=$it | DB 캐시 존재=$hasCached")
                    !hasCached
                }

                if (newToBeFetched.isNotEmpty()) {
                    // 항상 3년치 고정으로 가져옴
                    // → 기간 변경해도 DB에 이미 있어서 API 재호출 불필요
                    val endDate = LocalDate.now().toString()
                    val startDate = LocalDate.now().minusYears(3).toString()
                    Timber.d("[API] 가격 이력 API 호출 시작 | tickers=$newToBeFetched | 기간=$startDate ~ $endDate")

                    when (val result = simulationRepository.getEtfPriceHistories(
                        tickers = newToBeFetched,
                        startDate = startDate,
                        endDate = endDate
                    )) {
                        is BaseResult.Success -> {
                            val pointCounts = result.data.mapValues { it.value.content.size }
                            Timber.d("[API] 가격 이력 조회 성공 | 데이터 건수=$pointCounts")
                            simulationRepository.savePriceHistories(result.data)
                            Timber.d("[DB] 가격 이력 저장 완료 | tickers=${result.data.keys}")
                        }
                        is BaseResult.Error -> {
                            Timber.e("[API] 가격 이력 조회 실패 | message=${result.error.message}")
                            _simulationState.update { UiState.Error(result.error.message) }
                            return@launch
                        }
                    }
                } else {
                    Timber.d("[Cache] 모든 ticker DB 캐시 존재 → API 호출 스킵")
                }
            }

            _formState.update { current ->
                val items = current.portfolioItems.toMutableList()
                tickers.forEach { ticker ->
                    if (items.none { it.ticker == ticker }) {
                        items.add(PortfolioItem(ticker = ticker, name = ticker, weight = 0))
                    }
                }
                current.copy(portfolioItems = items)
            }
            Timber.d("[Portfolio] formState 업데이트 완료 | 포트폴리오=${_formState.value.portfolioItems.map { "${it.ticker}(${it.weight}%)" }}")

            triggerCalculation()
        }
    }

    fun onPortfolioItemRemoved(ticker: String) {
        Timber.d("[Portfolio] ETF 제거 | ticker=$ticker")
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
        val totalWeight = _formState.value.portfolioItems.sumOf { it.weight }
        Timber.d("[Weight] ticker=$ticker | 새 비중=${newWeight}% | 전체 합계=${totalWeight}%")
        triggerCalculation()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UI 이벤트
    // ─────────────────────────────────────────────────────────────────────────

    val idleGuideMessage: StateFlow<String> = _formState.map { form ->
        when {
            form.investmentAmount.isBlank() || form.investmentPeriod.isBlank() ->
                "투자 금액과 기간을 입력하면\n수익률 그래프가 나타납니다."
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
        Timber.d("[Form] 투자 방식 변경 | type=$type")
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

    fun onAiDialogDismiss() { _showAiDialog.value = false }

    private fun fetchAiDiagnosis() {
        if (_aiDiagnosisState.value is UiState.Loading ||
            _aiDiagnosisState.value is UiState.Success) return

        viewModelScope.launch {
            Timber.d("[AI] AI 진단 요청 시작")
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
            Timber.d("[AI] AI 진단 완료")
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 저장
    // ─────────────────────────────────────────────────────────────────────────

    fun onSaveIconClick() { _showSaveDialog.value = true }

    fun onSaveDialogDismiss() {
        _showSaveDialog.value = false
        _savePortfolioState.value = UiState.Idle
    }

    fun savePortfolio(portfolioName: String) {
        if (_savePortfolioState.value is UiState.Loading) return
        Timber.d("[Save] 포트폴리오 저장 요청 | name=$portfolioName")
        viewModelScope.launch {
            _savePortfolioState.update { UiState.Loading }
            delay(1000) // TODO: 실제 저장 API 호출
            _savePortfolioState.update { UiState.Success(Unit) }
            _showSaveDialog.value = false
            Timber.d("[Save] 포트폴리오 저장 완료 | name=$portfolioName")
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

            if (form.portfolioItems.isEmpty() || amount <= 0L || periodMonths <= 0) {
                Timber.d("[Calc] 입력 미완성 → Idle | portfolios=${form.portfolioItems.size}개 | amount=$amount | period=$periodMonths")
                _simulationState.update { UiState.Idle }
                return@launch
            }

            if (totalWeight != 100) {
                Timber.d("[Calc] 비중 합계 미달 → Loading 유지 | totalWeight=$totalWeight%")
                _simulationState.update { UiState.Loading }
                return@launch
            }

            Timber.d("[Calc] 계산 시작 | portfolios=${form.portfolioItems.map { "${it.ticker}(${it.weight}%)" }} | amount=$amount | period=${periodMonths}개월 | type=${form.investmentType}")
            _simulationState.update { UiState.Loading }

            val tickers = form.portfolioItems.map { it.ticker }
            val cachedHistories = simulationRepository.getCachedPriceHistories(tickers)
            val pointCounts = cachedHistories.mapValues { it.value.content.size }
            Timber.d("[DB] 캐시 조회 완료 | 데이터 건수=$pointCounts")

            when (val result = runSimulation(
                RunSimulationUseCase.Params(
                    portfolios = form.portfolioItems.toDomain(),
                    investmentAmount = amount,
                    investmentType = form.investmentType,
                    periodMonths = periodMonths,
                    priceHistories = cachedHistories
                )
            )) {
                is BaseResult.Success -> {
                    Timber.d("[Calc] 계산 성공 | estimatedFinalValue=${result.data.estimatedFinalValue} | totalReturn=${result.data.totalReturn}% | totalInvestment=${result.data.totalInvestment}")
                    _simulationState.update {
                        UiState.Success(result.data.toUiModel(form.investmentType))
                    }
                }
                is BaseResult.Error -> {
                    Timber.e("[Calc] 계산 실패 | message=${result.error.message}")
                    _simulationState.update { UiState.Error(result.error.message) }
                }
            }
        }
    }
}

data class SimulationFormState(
    val selectedTabIndex: Int = 0,
    val isOverlayEnabled: Boolean = false,
    val investmentType: InvestmentType = InvestmentType.INSTALLMENT,
    val investmentAmount: String = "",
    val investmentPeriod: String = "",
    val portfolioItems: List<PortfolioItem> = emptyList()
)