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

    private val _clusterState = MutableStateFlow<UiState<EtfClusterData>>(UiState.Loading)
    val clusterState: StateFlow<UiState<EtfClusterData>> = _clusterState.asStateFlow()

    private val _chartState = MutableStateFlow<UiState<EtfReturnChart>>(UiState.Idle)
    val chartState: StateFlow<UiState<EtfReturnChart>> = _chartState.asStateFlow()

    private val _periodReturn = MutableStateFlow<UiState<EtfPeriodReturn>>(UiState.Idle)
    val periodReturn: StateFlow<UiState<EtfPeriodReturn>> = _periodReturn.asStateFlow()

    private val _selectedPeriod = MutableStateFlow("ALL")
    val selectedPeriod: StateFlow<String> = _selectedPeriod.asStateFlow()

    private val _startDateMs        = MutableStateFlow<Long?>(null)
    private val _endDateMs          = MutableStateFlow<Long?>(null)
    private val _periodDurationDays = MutableStateFlow<Int?>(null)
    val startDateMs:        StateFlow<Long?> = _startDateMs.asStateFlow()
    val endDateMs:          StateFlow<Long?> = _endDateMs.asStateFlow()
    val periodDurationDays: StateFlow<Int?>  = _periodDurationDays.asStateFlow()

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
        loadCluster()
        loadPeriodReturn()
    }

    fun loadDetail() {
        viewModelScope.launch {
            _detailState.update { UiState.Loading }
            when (val result = etfRepository.getEtfDetail(ticker)) {
                is BaseResult.Success -> _detailState.update { UiState.Success(result.data) }
                is BaseResult.Error   -> _detailState.update { UiState.Error(result.error.message) }
            }
        }
    }

    fun loadCluster() {
        viewModelScope.launch {
            _clusterState.update { UiState.Loading }
            when (val result = etfRepository.getEtfCluster(ticker)) {
                is BaseResult.Success -> _clusterState.update { UiState.Success(result.data) }
                is BaseResult.Error   -> _clusterState.update { UiState.Error(result.error.message) }
            }
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
        _periodDurationDays.update { null }
    }

    fun loadChart(period: String = _selectedPeriod.value) {
        viewModelScope.launch {
            _chartState.update { UiState.Loading }
            val today = todayString()
            val (startDate, size) = if (period.isNotBlank()) {
                Pair(periodStartDate(period), periodSize(period))
            } else {
                val s = _startDateMs.value?.let { msToDateString(it) } ?: return@launch
                val days = ((_endDateMs.value ?: System.currentTimeMillis()) - (_startDateMs.value ?: 0L)) / 86_400_000
                Pair(s, (days + 5).toInt().coerceAtLeast(10))
            }
            val endDate = _endDateMs.value?.let { msToDateString(it) } ?: today
            when (val result = etfRepository.getEtfPriceHistory(ticker, startDate, endDate, size)) {
                is BaseResult.Success -> {
                    val points = result.data
                    _chartState.update {
                        UiState.Success(EtfReturnChart(
                            navData   = points.map { ChartPoint(it.date, it.nav.toDouble()) },
                            priceData = points.map { ChartPoint(it.date, it.stockPrice.toDouble()) },
                            kospiData = emptyList(),
                            sp500Data = emptyList(),
                        ))
                    }
                }
                is BaseResult.Error -> _chartState.update { UiState.Error(result.error.message) }
            }
        }
    }

    fun toggleNav()   { _showNav.update   { !it } }
    fun togglePrice() { _showPrice.update { !it } }
    fun toggleKospi() { _showKospi.update { !it } }
    fun toggleSp500() { _showSp500.update { !it } }

    private fun loadPeriodReturn() {
        viewModelScope.launch {
            val startDate = dateStringMonthsAgo(6)
            val today = todayString()
            when (val result = etfRepository.getEtfPriceHistory(ticker, startDate, today, size = 200)) {
                is BaseResult.Success -> {
                    val points = result.data
                    if (points.isEmpty()) return@launch
                    _periodReturn.update {
                        UiState.Success(EtfPeriodReturn(
                            nav1M   = calcReturn(points, 30,  isNav = true),
                            nav3M   = calcReturn(points, 90,  isNav = true),
                            nav6M   = calcReturn(points, 180, isNav = true),
                            index1M = 0.0, index3M = 0.0, index6M = 0.0,
                            price1M = calcReturn(points, 30,  isNav = false),
                            price3M = calcReturn(points, 90,  isNav = false),
                            price6M = calcReturn(points, 180, isNav = false),
                        ))
                    }
                }
                is BaseResult.Error -> Unit
            }
        }
    }

    private fun todayString(): String {
        val c = java.util.Calendar.getInstance()
        return "%04d-%02d-%02d".format(c.get(java.util.Calendar.YEAR), c.get(java.util.Calendar.MONTH) + 1, c.get(java.util.Calendar.DAY_OF_MONTH))
    }

    private fun msToDateString(ms: Long): String {
        val c = java.util.Calendar.getInstance().apply { timeInMillis = ms }
        return "%04d-%02d-%02d".format(c.get(java.util.Calendar.YEAR), c.get(java.util.Calendar.MONTH) + 1, c.get(java.util.Calendar.DAY_OF_MONTH))
    }

    private fun dateStringMonthsAgo(months: Int): String {
        val c = java.util.Calendar.getInstance().apply { add(java.util.Calendar.MONTH, -months) }
        return "%04d-%02d-%02d".format(c.get(java.util.Calendar.YEAR), c.get(java.util.Calendar.MONTH) + 1, c.get(java.util.Calendar.DAY_OF_MONTH))
    }

    private fun periodStartDate(period: String): String {
        val c = java.util.Calendar.getInstance()
        when (period) {
            "1W"  -> c.add(java.util.Calendar.WEEK_OF_YEAR, -1)
            "1M"  -> c.add(java.util.Calendar.MONTH, -1)
            "3M"  -> c.add(java.util.Calendar.MONTH, -3)
            "1Y"  -> c.add(java.util.Calendar.YEAR, -1)
            "3Y"  -> c.add(java.util.Calendar.YEAR, -3)
            "ALL" -> {
                val listingDate = (_detailState.value as? UiState.Success)?.data?.listingDate
                if (listingDate != null) return listingDate
                c.add(java.util.Calendar.YEAR, -20)
            }
        }
        return "%04d-%02d-%02d".format(c.get(java.util.Calendar.YEAR), c.get(java.util.Calendar.MONTH) + 1, c.get(java.util.Calendar.DAY_OF_MONTH))
    }

    private fun periodSize(period: String) = when (period) {
        "1W" -> 10; "1M" -> 35; "3M" -> 100; "1Y" -> 300; "3Y" -> 1000; else -> 5000
    }

    private fun calcReturn(points: List<EtfPriceData>, daysAgo: Int, isNav: Boolean): Double {
        if (points.size < 2) return 0.0
        val c = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -daysAgo) }
        val targetDate = "%04d-%02d-%02d".format(c.get(java.util.Calendar.YEAR), c.get(java.util.Calendar.MONTH) + 1, c.get(java.util.Calendar.DAY_OF_MONTH))
        val past = points.minByOrNull { kotlin.math.abs(it.date.compareTo(targetDate)) } ?: return 0.0
        val last = points.last()
        val pastVal = if (isNav) past.nav.toDouble() else past.stockPrice.toDouble()
        val lastVal  = if (isNav) last.nav.toDouble() else last.stockPrice.toDouble()
        if (pastVal == 0.0) return 0.0
        return (lastVal - pastVal) / pastVal * 100.0
    }
}
