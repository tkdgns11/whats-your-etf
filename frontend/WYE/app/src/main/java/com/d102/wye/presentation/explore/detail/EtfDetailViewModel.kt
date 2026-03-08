package com.d102.wye.presentation.explore.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.*
import com.d102.wye.domain.repository.EtfRepository
import com.d102.wye.presentation.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EtfDetailViewModel @Inject constructor(
    private val etfRepository: EtfRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val ticker: String = checkNotNull(savedStateHandle["ticker"])

    private val _detailState = MutableStateFlow<UiState<EtfDetail>>(UiState.Loading)
    val detailState: StateFlow<UiState<EtfDetail>> = _detailState.asStateFlow()

    private val _chartState = MutableStateFlow<UiState<EtfReturnChart>>(UiState.Idle)
    val chartState: StateFlow<UiState<EtfReturnChart>> = _chartState.asStateFlow()

    private val _periodReturn = MutableStateFlow<UiState<EtfPeriodReturn>>(UiState.Idle)
    val periodReturn: StateFlow<UiState<EtfPeriodReturn>> = _periodReturn.asStateFlow()

    private val _selectedPeriod = MutableStateFlow("ALL")
    val selectedPeriod: StateFlow<String> = _selectedPeriod.asStateFlow()

    private val _startDateMs        = MutableStateFlow<Long?>(null)
    private val _endDateMs          = MutableStateFlow<Long?>(null)
    private val _periodDurationDays = MutableStateFlow<Int?>(30) // 기본 1개월
    val startDateMs:        StateFlow<Long?> = _startDateMs.asStateFlow()
    val endDateMs:          StateFlow<Long?> = _endDateMs.asStateFlow()
    val periodDurationDays: StateFlow<Int?>  = _periodDurationDays.asStateFlow()

    // 차트 라인 표시 여부
    private val _showNav    = MutableStateFlow(true)
    private val _showPrice  = MutableStateFlow(true)
    private val _showKospi  = MutableStateFlow(false)
    private val _showSp500  = MutableStateFlow(false)
    val showNav:   StateFlow<Boolean> = _showNav.asStateFlow()
    val showPrice: StateFlow<Boolean> = _showPrice.asStateFlow()
    val showKospi: StateFlow<Boolean> = _showKospi.asStateFlow()
    val showSp500: StateFlow<Boolean> = _showSp500.asStateFlow()

    init {
        loadDetail()
        loadChart()
        loadPeriodReturn()
    }

    fun loadDetail() {
        viewModelScope.launch {
            _detailState.update { UiState.Loading }
//            when (val result = etfRepository.getEtfDetail(ticker)) {
//                is BaseResult.Success -> _detailState.update { UiState.Success(result.data) }
//                is BaseResult.Error   -> _detailState.update { UiState.Error(result.error.message) }
//            }
            _detailState.update { UiState.Success(mockEtfDetail(ticker)) }
        }
    }

    fun onPeriodSelected(period: String) {
        _selectedPeriod.update { period }
        val duration = when (period) {
            "1W" -> 7; "1M" -> 30; "3M" -> 90; "1Y" -> 365; "3Y" -> 3 * 365; else -> null
        }
        _periodDurationDays.update { duration }
        _startDateMs.update { null }
        _endDateMs.update { null }
    }

    fun onDateRangeSelected(start: Long, end: Long) {
        _startDateMs.update { start }
        _endDateMs.update { end }
        _selectedPeriod.update { "" }
        _periodDurationDays.update { null } // 커스텀 범위
        loadChart()
    }

    fun loadChart(period: String = _selectedPeriod.value) {
        viewModelScope.launch {
            _chartState.update { UiState.Loading }
//            when (val result = etfRepository.getEtfReturnChart(ticker, period)) {
//                is BaseResult.Success -> _chartState.update { UiState.Success(result.data) }
//                is BaseResult.Error   -> _chartState.update { UiState.Error(result.error.message) }
//            }
            _chartState.update { UiState.Success(mockReturnChart()) }
        }
    }

    fun toggleNav()   { _showNav.update   { !it } }
    fun togglePrice() { _showPrice.update { !it } }
    fun toggleKospi() { _showKospi.update { !it } }
    fun toggleSp500() { _showSp500.update { !it } }

    private fun loadPeriodReturn() {
        viewModelScope.launch {
//            when (val result = etfRepository.getEtfPeriodReturn(ticker)) {
//                is BaseResult.Success -> _periodReturn.update { UiState.Success(result.data) }
//                is BaseResult.Error   -> _periodReturn.update { UiState.Error(result.error.message) }
//            }
            _periodReturn.update { UiState.Success(mockPeriodReturn()) }
        }
    }
}

// ── 목업 데이터 ─────────────────────────────────────────────────

private fun mockEtfDetail(ticker: String) = EtfDetail(
    ticker = ticker,
    name = "KODEX 200",
    englishName = "KOSPI 200 Index Tracking Fund",
    riskLevel = 2,
    currentPrice = 35_420,
    iNav = 28_442,
    changeAmount = 450,
    changeRate = 1.61,
    iNavChangeAmount = -8,
    iNavChangeRate = -0.03,
    returnRate1M = 2.45,
    volume = 42_000,
    sectors = listOf(
        EtfSector(
            name = "반도체", percentage = 28.4,
            stocks = listOf(
                SectorStock("삼성전자", 25.0, "005930"),
                SectorStock("SK하이닉스", 15.2, "000660"),
                SectorStock("LG에너지솔루션", 8.4, "373220"),
                SectorStock("삼성바이오로직스", 5.8, "207940"),
                SectorStock("현대차", 4.2, "005380"),
            ),
            aiAnalysis = "반도체 섹터의 높은 기여도로 인해 IT 업황 회복 시 강한 반등이 예상됩니다. 단, 상위 2개 종목 비중이 80% 이상으로 집중도가 높은 점을 유의하세요.",
        ),
        EtfSector(
            name = "금융", percentage = 15.5,
            stocks = listOf(
                SectorStock("KB금융", 22.0, "105560"),
                SectorStock("신한지주", 18.5, "055550"),
                SectorStock("하나금융지주", 14.0, "086790"),
            ),
            aiAnalysis = "금리 인하 기조에 따라 은행주 수익성에 영향이 예상됩니다.",
        ),
        EtfSector(
            name = "자동차", percentage = 12.1,
            stocks = listOf(
                SectorStock("현대차", 35.0, "005380"),
                SectorStock("기아", 28.0, "000270"),
                SectorStock("현대모비스", 15.0, "012330"),
            ),
            aiAnalysis = "전기차 전환 속도가 수익성에 중요한 변수입니다.",
        ),
        EtfSector(
            name = "서비스", percentage = 8.2,
            stocks = listOf(
                SectorStock("NAVER", 30.0, "035420"),
                SectorStock("카카오", 22.0, "035720"),
            ),
            aiAnalysis = "플랫폼 광고 시장 회복 여부가 주요 관전 포인트입니다.",
        ),
        EtfSector(
            name = "화학", percentage = 7.6,
            stocks = listOf(
                SectorStock("LG화학", 28.0, "051910"),
                SectorStock("롯데케미칼", 18.0, "011170"),
            ),
            aiAnalysis = "글로벌 유가 변동이 원가에 직접 영향을 미칩니다.",
        ),
    ),
    influentialStocks = listOf(
        InfluentialStock("005930", "삼성전자", 24.50, 74_200, 2.8),
        InfluentialStock("000660", "SK 하이닉스", 5.80, 162_100, -1.2),
        InfluentialStock("035420", "NAVER", 3.20, 198_500, 0.5),
        InfluentialStock("005380", "현대차", 2.90, 215_000, -0.3),
    ),
    manager = "삼성자산운용",
    volatility = "",
    expenseRatio = 0.0062,
    netAsset = 2_451_200_000_000L,
    listedDate = "2002.10.14",
)

private fun mockReturnChart(): EtfReturnChart {
    val baseValues = listOf(100.0, 101.2, 100.8, 102.1, 103.5, 102.9, 104.2, 105.1, 104.7, 106.3, 107.0, 108.2, 107.8, 109.1)
    fun toPoints(values: List<Double>) = values.mapIndexed { i, v ->
        ChartPoint("2026-02-${(i + 1).toString().padStart(2, '0')}", v)
    }
    return EtfReturnChart(
        navData   = toPoints(baseValues),
        priceData = toPoints(baseValues.map { it - 0.5 }),
        kospiData = toPoints(baseValues.map { it * 0.97 }),
        sp500Data = toPoints(baseValues.map { it * 1.02 }),
    )
}

private fun mockPeriodReturn() = EtfPeriodReturn(
    nav1M = 0.40,   nav3M = -0.09,  nav6M = 12.56,
    index1M = 0.42, index3M = -0.02, index6M = 12.73,
    price1M = -0.37, price3M = -1.31, price6M = 10.61,
)
