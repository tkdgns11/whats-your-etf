package com.d102.wye.presentation.explore.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d102.wye.domain.common.BaseResult
import com.d102.wye.domain.model.Etf
import com.d102.wye.domain.model.EtfFilter
import com.d102.wye.domain.repository.EtfRepository
import com.d102.wye.domain.repository.UserRepository
import com.d102.wye.domain.state.EtfFilterState
import com.d102.wye.presentation.model.EtfListItemUiModel
import com.d102.wye.presentation.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val etfRepository: EtfRepository,
    private val userRepository: UserRepository,
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
    private var currentPage = 0
    private var isLastPage = false
    private var isDataInitialized = false

    private val _selectedTickers = MutableStateFlow<Set<String>>(emptySet())
    val selectedTickers: StateFlow<Set<String>> = _selectedTickers.asStateFlow()

    init {
        observeFavoriteChanges()
        loadEtfList()
    }

    private fun observeFavoriteChanges() {
        viewModelScope.launch {
            userRepository.favoriteEtfChanged.collectLatest {
                if (rawEtfList.isEmpty()) return@collectLatest

                rawEtfList = syncFavoriteStates(rawEtfList)
                applyFilter()
            }
        }
    }

    fun loadEtfList(filter: EtfFilterState = _filterState.value) {
        viewModelScope.launch {
            _uiState.update { UiState.Loading }
            currentPage = 0
            isLastPage = false
            rawEtfList = emptyList()
            isDataInitialized = false

            when (val result = etfRepository.getEtfList(filter.toFilter(_sortedBy.value), page = 0)) {
                is BaseResult.Success -> {
                    isLastPage = result.data.isLast
                    rawEtfList = syncFavoriteStates(result.data.items.map { it.toUiModel() })
                    isDataInitialized = true
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
                    rawEtfList = syncFavoriteStates(
                        (rawEtfList + result.data.items.map { it.toUiModel() })
                        .distinctBy { it.ticker }
                    )
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
            Timber.d(
                "[FavoriteEtf] toggle requested | etfId=${target.etfId} | ticker=${target.ticker} | isLiked=${target.isLiked}"
            )
            val result = if (target.isLiked) {
                userRepository.deleteFavoriteEtf(target.ticker)
            } else {
                userRepository.addFavoriteEtf(target.ticker)
            }

            when (result) {
                is BaseResult.Success -> {
                    Timber.d(
                        "[FavoriteEtf] toggle succeeded | etfId=${target.etfId} | ticker=${target.ticker} | newIsLiked=${!target.isLiked}"
                    )
                    rawEtfList = rawEtfList.map { item ->
                        if (item.ticker == ticker) item.copy(isLiked = !item.isLiked) else item
                    }
                    applyFilter()
                }
                is BaseResult.Error -> {
                    Timber.e(
                        "[FavoriteEtf] toggle failed | etfId=${target.etfId} | ticker=${target.ticker} | message=${result.error.message}"
                    )
                    _uiState.update { UiState.Error(result.error.message) }
                }
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

    private suspend fun syncFavoriteStates(items: List<EtfListItemUiModel>): List<EtfListItemUiModel> =
        coroutineScope {
            items.map { item ->
                async {
                    when (val result = userRepository.checkFavoriteEtf(item.ticker)) {
                        is BaseResult.Success -> item.copy(isLiked = result.data)
                        is BaseResult.Error -> {
                            Timber.w(
                                "[FavoriteEtf] check failed | ticker=${item.ticker} | message=${result.error.message}"
                            )
                            item
                        }
                    }
                }
            }.awaitAll()
        }

    private fun applyFilter() {
        if (!isDataInitialized) return
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
private fun Etf.toUiModel() = EtfListItemUiModel(
    etfId = etfId,
    ticker = ticker,
    name = name,
    currentPrice = currentPrice,
    changeRate = changeRate,
    changeAmount = changeAmount,
    riskType = riskType,
    isLiked = isFavorite,
)

data class ExploreData(
    val etfList: List<EtfListItemUiModel>,
    val filteredList: List<EtfListItemUiModel>,
    val filter: EtfFilterState,
)
