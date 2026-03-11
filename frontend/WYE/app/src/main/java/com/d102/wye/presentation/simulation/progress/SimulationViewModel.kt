package com.d102.wye.presentation.simulation.progress

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d102.wye.domain.model.AiDiagnosisResult
import com.d102.wye.domain.state.InvestmentType
import com.d102.wye.presentation.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SimulationViewModel @Inject constructor(
    // TODO: Repository 주입
    // private val simulationRepository: SimulationRepository
) : ViewModel() {

    // 투자 설정 상태
    private val _formState = MutableStateFlow(SimulationFormState())
    val formState: StateFlow<SimulationFormState> = _formState.asStateFlow()

    // 결과 상태
    private val _resultState = MutableStateFlow<UiState<SimulationResult>>(UiState.Idle)
    val resultState: StateFlow<UiState<SimulationResult>> = _resultState.asStateFlow()

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


    // ── 포트폴리오 아이템 추가/수정/삭제 ────────────────────────────────

    fun addPortfolioItems(tickers: List<String>) {
        _formState.update { currentState ->
            val currentItems = currentState.portfolioItems.toMutableList()

            tickers.forEach { ticker ->
                // 이미 담겨 있는 종목은 무시
                if (currentItems.none { it.ticker == ticker }) {
                    // TODO: 실제 API에서는 ticker로 name을 조회해야 합니다.
                    // 지금은 임시로 ticker를 이름으로 사용
                    currentItems.add(PortfolioItem(ticker = ticker, name = ticker, weight = 0))
                }
            }
            currentState.copy(portfolioItems = currentItems)
        }
        calculateSimulation()
    }


    fun onPortfolioItemRemoved(ticker: String) {
        _formState.update { currentState ->
            currentState.copy(
                portfolioItems = currentState.portfolioItems.filter { it.ticker != ticker }
            )
        }
        calculateSimulation()
    }

    fun updateItemWeight(ticker: String, newWeight: Int) {
        _formState.update { currentState ->
            val updatedItems = currentState.portfolioItems.map { item ->
                // 비중은 0~100 사이로 제한
                if (item.ticker == ticker) item.copy(weight = newWeight.coerceIn(0, 100))
                else item
            }
            currentState.copy(portfolioItems = updatedItems)
        }
        // 비중이 바뀌면 차트 결과도 갱신되어야 함
        calculateSimulation()
    }

    // ── UI 상호작용 ────────────────────────────────

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

    // ── AI 진단 & 저장 로직 (기존 코드 유지) ────────────────────────────────

    fun onAiDiagnosisClick() {
        _showAiDialog.value = true
        fetchAiDiagnosis()
    }

    fun onAiDialogDismiss() {
        _showAiDialog.value = false
    }

    private fun fetchAiDiagnosis() {
        if (_aiDiagnosisState.value is UiState.Loading || _aiDiagnosisState.value is UiState.Success) return

        viewModelScope.launch {
            _aiDiagnosisState.update { UiState.Loading }
            delay(1500)
            _aiDiagnosisState.update {
                UiState.Success(
                    AiDiagnosisResult(
                        mainTitle = "공격적인 수익 추구!",
                        subTitle = "기술주 중심의 로켓 포트폴리오 🚀",
                        tags = listOf("기술주집중", "고변동성", "성장중심"),
                        feedback = "현재 포트폴리오는 특정 섹터에 집중되어 있어 시장 상황에 따른 변동성이 매우 큽니다. 장기적인 안정을 위해 자산의 일부를 배당형 ETF로 분산투자하는 것을 고려해보세요."
                    )
                )
            }
        }
    }

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
            delay(1000)
            _savePortfolioState.update { UiState.Success(Unit) }
            _showSaveDialog.value = false
        }
    }

    // ── 계산 ────────────────────────────────

    private fun calculateSimulation() {
        val form = _formState.value

        if (form.investmentAmount.isBlank() ||
            form.investmentPeriod.isBlank() ||
            form.portfolioItems.isEmpty()
        ) {
            _resultState.update { UiState.Idle }
            return
        }

        viewModelScope.launch {
            _resultState.update { UiState.Loading }
            // TODO: 실제 계산
        }
    }
}

// ─────────────────────────────────────────
// 데이터 모델
// ─────────────────────────────────────────

data class PortfolioItem(
    val ticker: String,
    val name: String,
    val weight: Int
)

data class SimulationFormState(
    val selectedTabIndex: Int = 0,
    val isOverlayEnabled: Boolean = false,
    val investmentType: InvestmentType = InvestmentType.INSTALLMENT,
    val investmentAmount: String = "",
    val investmentPeriod: String = "",
    val portfolioItems: List<PortfolioItem> = emptyList()
)

data class SimulationResult(
    val estimatedFinalAsset: String,
    val yieldRate: String,
    val totalInvestment: String,
    val per: String,
    val pbr: String,
    val roe: String
)