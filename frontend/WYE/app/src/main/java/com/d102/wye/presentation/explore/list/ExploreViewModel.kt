package com.d102.wye.presentation.explore.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.Etf
import com.d102.wye.domain.model.EtfFilter
import com.d102.wye.domain.model.EtfLikeData
import com.d102.wye.domain.repository.EtfRepository
import com.d102.wye.domain.state.EtfFilterState
import com.d102.wye.presentation.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val etfRepository: EtfRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<ExploreData>>(UiState.Idle)
    val uiState: StateFlow<UiState<ExploreData>> = _uiState.asStateFlow()

    private val _filterState = MutableStateFlow(EtfFilterState())
    val filterState: StateFlow<EtfFilterState> = _filterState.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _sortedBy = MutableStateFlow<String?>(null)
    val sortedBy: StateFlow<String?> = _sortedBy.asStateFlow()

    private var rawEtfList: List<EtfListItemUiModel> = emptyList()
    private var likedTickers: Set<String> = emptySet()
    private var currentPage = 0
    private var isLastPage = false

    private val _selectedTickers = MutableStateFlow<Set<String>>(emptySet())
    val selectedTickers: StateFlow<Set<String>> = _selectedTickers.asStateFlow()

    init {
        observeLikedEtfs()
        loadEtfList()
    }

    fun loadEtfList(filter: EtfFilterState = _filterState.value) {
        viewModelScope.launch {
            _uiState.update { UiState.Loading }
            currentPage = 0
            isLastPage = false
            rawEtfList = emptyList()

            when (val result = etfRepository.getEtfList(filter.toFilter(_sortedBy.value), page = 0)) {
                is BaseResult.Success -> {
                    isLastPage = result.data.isLast
                    rawEtfList = result.data.items.map { it.toUiModel(likedTickers) }
                    applyFilter()
                }
                is BaseResult.Error -> _uiState.update { UiState.Error(result.error.message) }
            }
        }
    }

    fun loadMore() {
        if (isLastPage || _isLoadingMore.value) return
        viewModelScope.launch {
            _isLoadingMore.update { true }
            val nextPage = currentPage + 1
            when (val result = etfRepository.getEtfList(_filterState.value.toFilter(_sortedBy.value), page = nextPage)) {
                is BaseResult.Success -> {
                    currentPage = nextPage
                    isLastPage = result.data.isLast
                    rawEtfList = (rawEtfList + result.data.items.map { it.toUiModel(likedTickers) })
                        .distinctBy { it.ticker }
                    applyFilter()
                }
                is BaseResult.Error -> _uiState.update { UiState.Error(result.error.message) }
            }
            _isLoadingMore.update { false }
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

    fun onSortChanged(sortedBy: String?) {
        _sortedBy.update { sortedBy }
        loadEtfList()
    }

    fun onFilterChanged(filter: EtfFilterState) {
        _filterState.update { filter }
        loadEtfList(filter)
    }

    fun onLikeToggled(ticker: String) {
        val target = rawEtfList.firstOrNull { it.ticker == ticker } ?: return
        viewModelScope.launch {
            when (val result = etfRepository.toggleLike(target.toEtfLikeData())) {
                is BaseResult.Success -> Unit
                is BaseResult.Error -> _uiState.update { UiState.Error(result.error.message) }
            }
        }
    }

    fun toggleSelection(ticker: String) {
        _selectedTickers.update { currentSet ->
            Timber.d("$ticker selected")
            if (currentSet.contains(ticker)) currentSet - ticker else currentSet + ticker
        }
    }

    fun removeSelection(ticker: String) {
        _selectedTickers.update { it - ticker }
    }

    fun clearSelection() {
        _selectedTickers.update { emptySet() }
    }

    private fun observeLikedEtfs() {
        viewModelScope.launch {
            etfRepository.getLikedEtfList().collect { likedEtfs ->
                likedTickers = likedEtfs.mapTo(mutableSetOf()) { it.ticker }
                syncLikeState()
            }
        }
    }

    private fun syncLikeState() {
        if (rawEtfList.isEmpty()) return
        rawEtfList = rawEtfList.map { it.copy(isLiked = likedTickers.contains(it.ticker)) }
        applyFilter()
    }

    private fun applyFilter() {
        val filter = _filterState.value
        val filtered = if (filter.query.isBlank()) {
            rawEtfList
        } else {
            rawEtfList.filter {
                when (filter.searchScope) {
                    "etf"   -> it.name.contains(filter.query, ignoreCase = true)
                    "stock" -> it.ticker.contains(filter.query, ignoreCase = true)
                    else    -> it.name.contains(filter.query, ignoreCase = true) ||
                            it.ticker.contains(filter.query, ignoreCase = true)
                }
            }
        }
        _uiState.update {
            UiState.Success(ExploreData(etfList = rawEtfList, filteredList = filtered, filter = filter))
        }
    }
}

// ─── EtfFilterState → EtfFilter 변환 ────────────────────────────
private fun EtfFilterState.toFilter(sortedBy: String? = null) = EtfFilter(
    riskType = riskType,
    strategy = strategy,
    isDerivatives = hasDerivative,
    isLeverage = hasLeverage,
    isInverse = hasInverse,
    sortedBy = sortedBy,
)

// ─── Domain → UiModel 변환 ───────────────────────────────────────
private fun Etf.toUiModel(likedTickers: Set<String>) = EtfListItemUiModel(
    etfId = etfId,
    ticker = ticker,
    name = name,
    currentPrice = currentPrice,
    changeRate = changeRate,
    changeAmount = changeAmount,
    riskType = riskType,
    isLiked = likedTickers.contains(ticker) || isFavorite,
)

// ─── UiModel → EtfLikeData 변환 (toggleLike 전용) ────────────────
private fun EtfListItemUiModel.toEtfLikeData() = EtfLikeData(
    ticker = ticker,
    name = name,
    currentPrice = currentPrice,
    changeRate = changeRate,
    changeAmount = changeAmount,
    riskType = riskType,
)

data class ExploreData(
    val etfList: List<EtfListItemUiModel>,
    val filteredList: List<EtfListItemUiModel>,
    val filter: EtfFilterState,
)

data class EtfListItemUiModel(
    val etfId: Long,
    val ticker: String,
    val name: String,
    val currentPrice: Long,
    val changeRate: Double,
    val changeAmount: Long,
    val riskType: String,
    val isLiked: Boolean,
)
