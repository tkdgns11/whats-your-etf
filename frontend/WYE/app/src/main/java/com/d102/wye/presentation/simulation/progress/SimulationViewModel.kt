package com.d102.wye.presentation.simulation.progress

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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SimulationSetupViewModel @Inject constructor(
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

    fun onAiDiagnosisClick() {
        _showAiDialog.value = true
        fetchAiDiagnosis()
    }

    fun onAiDialogDismiss() {
        _showAiDialog.value = false
        // 다이얼로그를 닫을 때 상태를 초기화할지, 캐싱해둘지 결정
        // _aiDiagnosisState.value = UiState.Idle
    }

    private fun fetchAiDiagnosis() {
        // 이미 로딩 중이거나 성공했다면 중복 호출 방지 (필요에 따라 뺄 수 있음)
        if (_aiDiagnosisState.value is UiState.Loading || _aiDiagnosisState.value is UiState.Success) {
            return
        }

        viewModelScope.launch {
            _aiDiagnosisState.update { UiState.Loading }

            // TODO: 실제 AI 진단 API 통신 로직 연결
            // when (val result = aiRepository.getDiagnosis(portfolio = _formState.value.portfolioItems)) {
            //     is BaseResult.Success -> _aiDiagnosisState.update { UiState.Success(result.data.toAiDiagnosisResult()) }
            //     is BaseResult.Error   -> _aiDiagnosisState.update { UiState.Error(result.error.message) }
            // }

            // 임시 딜레이 및 목데이터 연동 (테스트용)
            delay(1500)
            _aiDiagnosisState.update {
                UiState.Success(
                    AiDiagnosisResult(
                        mainTitle = "공격적인 수익 추구!",
                        subTitle = "기술주 중심의 로켓 포트폴리오 \uD83D\uDE80",
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
        _savePortfolioState.value = UiState.Idle // 다이얼로그 닫을 때 통신 상태 초기화
    }

    fun savePortfolio(portfolioName: String) {
        // 이미 저장 중이면 중복 클릭 방지
        if (_savePortfolioState.value is UiState.Loading) return

        viewModelScope.launch {
            _savePortfolioState.update { UiState.Loading }

            // TODO: 실제 저장 API 호출 로직 연결
            // val currentForm = _formState.value
            // val response = simulationRepository.savePortfolio(
            //     name = portfolioName,
            //     items = currentForm.portfolioItems
            // )
            // when (response) { ... }

            // API 통신 테스트용 딜레이
            delay(1000)

            _savePortfolioState.update { UiState.Success(Unit) }
            _showSaveDialog.value = false // 저장이 성공하면 다이얼로그를 닫아줍니다.
        }
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
    val totalInvestment: String,
    val per: String,
    val pbr: String,
    val roe: String
)