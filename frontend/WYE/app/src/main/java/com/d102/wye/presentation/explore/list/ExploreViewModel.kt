package com.d102.wye.presentation.explore.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d102.wye.domain.model.Etf
import com.d102.wye.domain.common.BaseResult
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
import timber.log.Timber
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
    private var likedTickers: Set<String> = emptySet()

    // 다중 선택 상태를 관리하는 StateFlow 추가
    private val _selectedTickers = MutableStateFlow<Set<String>>(emptySet())
    val selectedTickers: StateFlow<Set<String>> = _selectedTickers.asStateFlow()

    init {
        observeLikedEtfs()
        loadEtfList()
    }

    fun loadEtfList() {
        viewModelScope.launch {
            _uiState.update { UiState.Loading }
//            etfRepository.getEtfList().collect { etfList ->
//                rawEtfList = etfList
//                applyFilter()
//            }

    // TODO: API 연동 시 mockEtfList 대신 etfRepository.getEtfList()를 collect해서 사용한다.
            mockEtfList = listOf(
                EtfListItemUiModel("069500", "KODEX 200", 32_450, 1.24, 400, 1, likedTickers.contains("069500")),
                EtfListItemUiModel("091160", "KODEX 반도체", 42_350, 2.15, 890, 4, likedTickers.contains("091160")),
                EtfListItemUiModel("102780", "KODEX 인버스", 2_145, -1.20, -26, 5, likedTickers.contains("102780")),
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
        val target = mockEtfList.firstOrNull { it.ticker == ticker } ?: return
        viewModelScope.launch {
            when (val result = etfRepository.toggleLike(target.toDomain())) {
                is BaseResult.Success -> Unit
                is BaseResult.Error -> _uiState.update { UiState.Error(result.error.message) }
            }
        }
    }

    fun toggleSelection(ticker: String) {
        _selectedTickers.update { currentSet ->
            Timber.d("$ticker selected")
            if (currentSet.contains(ticker)) {
                currentSet - ticker // 있으면 제거
            } else {
                currentSet + ticker // 없으면 추가
            }
        }
    }

    fun removeSelection(ticker: String) {
        _selectedTickers.update { it - ticker }
    }

    fun clearSelection() {
        _selectedTickers.update { emptySet() }
    }

    fun initializeSelection(tickers: List<String>) {
        _selectedTickers.update { tickers.toSet() }
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
        if (mockEtfList.isEmpty()) return
        mockEtfList = mockEtfList.map { it.copy(isLiked = likedTickers.contains(it.ticker)) }
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
    isLiked = false,  // TODO: API 연동 시 관심 ETF 상태를 서버 응답 또는 로컬 캐시와 함께 매핑
)

private fun EtfListItemUiModel.toDomain() = Etf(
    ticker = ticker,
    name = name,
    currentPrice = currentPrice,
    changeRate = changeRate,
    changeAmount = changeAmount,
    volume = 0L,
    riskLevel = riskLevel,
    investmentStrategy = "",
    assetClass = "",
    theme = "",
    dividendRate = 0.0,
    dividendCycle = "",
    hasDerivative = false,
    per = 0.0,
    pbr = 0.0,
    roe = 0.0,
    expenseRatio = 0.0,
    netAsset = 0L,
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
