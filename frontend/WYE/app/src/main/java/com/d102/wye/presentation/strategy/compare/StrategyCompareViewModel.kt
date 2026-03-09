package com.d102.wye.presentation.strategy.compare

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class StrategyCompareViewModel @Inject constructor() : ViewModel() {

    private val palette = listOf(
        Color(0xFF4B6B4E),
        Color(0xFFE57A3C),
        Color(0xFF6B8ED8)
    )

    private val _uiState = MutableStateFlow<UiState<CompareData>>(UiState.Idle)
    val uiState: StateFlow<UiState<CompareData>> = _uiState.asStateFlow()

    init {
        loadCompareData()
    }

    private fun loadCompareData() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            delay(500)
            _uiState.value = UiState.Success(
                CompareData(
                    strategyList = listOf(
                        CompareStrategyItem(1L, "기술주 몰빵 전략", "+15.2%"),
                        CompareStrategyItem(2L, "안전제일 배당왕", "+8.4%"),
                        CompareStrategyItem(3L, "적립식 3년 집중", "+12.1%")
                    ),
                    detailStats = mapOf(
                        1L to CompareDetailStat(1L, "+15.2%", "-18.4%", "14.2%"),
                        2L to CompareDetailStat(2L, "+8.4%", "-4.2%", "5.8%"),
                        3L to CompareDetailStat(3L, "+12.1%", "-12.5%", "9.4%")
                    )
                )
            )
        }
    }

    fun toggleSelection(id: Long) {
        val current = (_uiState.value as? UiState.Success)?.data ?: return
        _uiState.update {
            val list = current.strategyList
            val currentSelectedCount = list.count { it.isSelected }
            val targetItem = list.find { it.id == id } ?: return

            if (!targetItem.isSelected && currentSelectedCount >= 3) return

            val updated = list.map { item ->
                if (item.id == id) item.copy(isSelected = !item.isSelected) else item
            }.let { updatedList ->
                var colorIndex = 0
                updatedList.map {
                    if (it.isSelected) it.copy(color = palette[colorIndex++ % palette.size])
                    else it.copy(color = Color.Transparent)
                }
            }
            UiState.Success(current.copy(strategyList = updated))
        }
    }
}

data class CompareData(
    val strategyList: List<CompareStrategyItem>,
    val detailStats: Map<Long, CompareDetailStat>
)

data class CompareStrategyItem(
    val id: Long,
    val name: String,
    val returnRate: String,
    val isSelected: Boolean = false,
    val color: Color = Color.Transparent
)

data class CompareDetailStat(
    val id: Long,
    val cumulativeReturn: String,
    val dividendYield: String,
    val volatility: String
)