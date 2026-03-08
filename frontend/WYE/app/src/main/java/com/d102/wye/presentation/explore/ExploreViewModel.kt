package com.d102.wye.presentation.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d102.wye.domain.model.Etf
import com.d102.wye.domain.repository.EtfRepository
import com.d102.wye.domain.state.EtfFilterState
import com.d102.wye.domain.usecase.etf.FilterEtfListUseCase
import com.d102.wye.presentation.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val etfRepository: EtfRepository,
    private val filterEtfListUseCase: FilterEtfListUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<ExploreData>>(UiState.Idle)
    val uiState: StateFlow<UiState<ExploreData>> = _uiState.asStateFlow()

    private val _filterState = MutableStateFlow(EtfFilterState())
    val filterState: StateFlow<EtfFilterState> = _filterState.asStateFlow()

    private var rawEtfList: List<Etf> = emptyList()
    private var mockEtfList: List<EtfListItemUiModel> = emptyList()

    init {
        loadEtfList()
    }

    fun loadEtfList() {
        viewModelScope.launch {
            _uiState.update { UiState.Loading }
//            etfRepository.getEtfList().collect { etfList ->
//                rawEtfList = etfList
//                applyFilter()
//            }

            mockEtfList = listOf(
                EtfListItemUiModel("KODEX200", "KODEX 200", 32_450, 1.24, 400, 1, false),
                EtfListItemUiModel("TIGER나스닥", "TIGER 미국나스닥100", 104_820, 2.15, 2_210, 1, false),
                EtfListItemUiModel("ACESP500", "ACE 미국S&P500", 15_340, 0.85, 130, 1, false),
                EtfListItemUiModel("SOL배당", "SOL 미국배당다우존스", 10_120, 0.42, 45, 3, false),
                EtfListItemUiModel("KODEX인버스", "KODEX 200선물인버스2X", 2_145, -2.31, -50, 5, false),
                EtfListItemUiModel("TIGER나스닥2", "TIGER 미국나스닥100(레버리지)", 104_820, 2.15, 2_210, 1, false),
                EtfListItemUiModel("SOL배당2", "SOL 미국배당다우존스(월배당)", 10_120, 0.42, 45, 1, false),
            )
            applyFilter()
        }
    }

    fun onQueryChanged(query: String) {
        _filterState.update { it.copy(query = query) }
        applyFilter()
    }

    fun onSearchScopeSelected(scope: String?) {
        _filterState.update { it.copy(searchScope = scope) }
        applyFilter()
    }

    fun onAssetClassSelected(assetClass: String?) {
        _filterState.update { it.copy(assetClass = assetClass) }
        applyFilter()
    }

    fun onFilterChanged(filter: EtfFilterState) {
        _filterState.update { filter }
        applyFilter()
    }

    fun onLikeToggled(ticker: String) {
        mockEtfList = mockEtfList.map {
            if (it.ticker == ticker) it.copy(isLiked = !it.isLiked) else it
        }
        applyFilter()
    }

    private fun applyFilter() {
        val filter = _filterState.value
        // TODO: 실제 API 연동 시 rawEtfList 사용으로 교체
        // val filtered = filterEtfListUseCase(rawEtfList, filter).map { it.toUiModel() }
        // val all = rawEtfList.map { it.toUiModel() }
        val filtered = if (filter.query.isBlank()) {
            mockEtfList
        } else {
            mockEtfList.filter {
                when (filter.searchScope) {
                    "etf"   -> it.name.contains(filter.query, ignoreCase = true)
                    "stock" -> it.ticker.contains(filter.query, ignoreCase = true)
                    else    -> it.name.contains(filter.query, ignoreCase = true) ||
                               it.ticker.contains(filter.query, ignoreCase = true)
                }
            }
        }
        _uiState.update {
            UiState.Success(ExploreData(etfList = mockEtfList, filteredList = filtered, filter = filter))
        }
    }
}

// ─── presentation 전용 변환 ───────────────────────────────────────
private fun Etf.toUiModel() = EtfListItemUiModel(
    ticker = ticker,
    name = name,
    currentPrice = currentPrice,
    changeRate = changeRate,
    changeAmount = changeAmount,
    riskLevel = riskLevel,
    isLiked = false,  // TODO: Room 관심 ETF 연동
)

data class ExploreData(
    val etfList: List<EtfListItemUiModel>,
    val filteredList: List<EtfListItemUiModel>,
    val filter: EtfFilterState,
)

data class EtfListItemUiModel(
    val ticker: String,
    val name: String,
    val currentPrice: Long,
    val changeRate: Double,
    val changeAmount: Long,
    val riskLevel: Int,
    val isLiked: Boolean,
)
