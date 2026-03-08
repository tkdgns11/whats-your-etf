package com.d102.wye.presentation.explore.stock

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.Stock
import com.d102.wye.domain.repository.StockRepository
import com.d102.wye.presentation.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StockDetailViewModel @Inject constructor(
    private val stockRepository: StockRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val ticker: String = checkNotNull(savedStateHandle["ticker"])

    private val _stockState = MutableStateFlow<UiState<Stock>>(UiState.Loading)
    val stockState: StateFlow<UiState<Stock>> = _stockState.asStateFlow()

    init {
        loadStock()
    }

    fun loadStock() {
        viewModelScope.launch {
            _stockState.update { UiState.Loading }
            when (val result = stockRepository.getStock(ticker)) {
                is BaseResult.Success -> _stockState.update { UiState.Success(result.data) }
                is BaseResult.Error   -> _stockState.update { UiState.Error(result.error.message) }
            }
        }
    }
}
