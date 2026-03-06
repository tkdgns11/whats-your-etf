package com.d102.wye.presentation.explore.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.EtfDetail
import com.d102.wye.domain.model.EtfPeriodReturn
import com.d102.wye.domain.model.EtfReturnChart
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

    private val _selectedPeriod = MutableStateFlow("1W")
    val selectedPeriod: StateFlow<String> = _selectedPeriod.asStateFlow()

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

    fun onPeriodSelected(period: String) {
        _selectedPeriod.update { period }
        loadChart(period)
    }

    fun loadChart(period: String = _selectedPeriod.value) {
        viewModelScope.launch {
            _chartState.update { UiState.Loading }
            when (val result = etfRepository.getEtfReturnChart(ticker, period)) {
                is BaseResult.Success -> _chartState.update { UiState.Success(result.data) }
                is BaseResult.Error   -> _chartState.update { UiState.Error(result.error.message) }
            }
        }
    }

    fun toggleNav()   { _showNav.update   { !it } }
    fun togglePrice() { _showPrice.update { !it } }
    fun toggleKospi() { _showKospi.update { !it } }
    fun toggleSp500() { _showSp500.update { !it } }

    private fun loadPeriodReturn() {
        viewModelScope.launch {
            when (val result = etfRepository.getEtfPeriodReturn(ticker)) {
                is BaseResult.Success -> _periodReturn.update { UiState.Success(result.data) }
                is BaseResult.Error   -> _periodReturn.update { UiState.Error(result.error.message) }
            }
        }
    }
}
