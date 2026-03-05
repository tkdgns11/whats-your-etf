package com.d102.wye.presentation.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d102.wye.domain.state.EtfFilterState
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
    // TODO: Repository / UseCase 주입
    // private val etfRepository: EtfRepository,
    // private val filterEtfListUseCase: FilterEtfListUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<ExploreData>>(UiState.Idle)
    val uiState: StateFlow<UiState<ExploreData>> = _uiState.asStateFlow()

    // 필터 상태 (UiState와 별도로 관리 — 필터 변경이 전체 로딩을 트리거하지 않도록)
    private val _filterState = MutableStateFlow(EtfFilterState())
    val filterState: StateFlow<EtfFilterState> = _filterState.asStateFlow()

    init {
        loadEtfList()
    }

    fun loadEtfList() {
        viewModelScope.launch {
            _uiState.update { UiState.Loading }

            // TODO: 1분 polling Flow 구독
            // etfRepository.getEtfList().collect { etfList ->
            //     val uiModels = etfList.map { it.toEtfListItemUiModel() }
            //     val filtered = filterEtfListUseCase(etfList, _filterState.value)
            //         .map { it.toEtfListItemUiModel() }
            //     _uiState.update {
            //         UiState.Success(
            //             ExploreData(
            //                 etfList = uiModels,
            //                 filteredList = filtered,
            //                 filter = _filterState.value
            //             )
            //         )
            //     }
            // }
        }
    }

    fun onQueryChanged(query: String) {
        _filterState.update { it.copy(query = query) }
        applyFilter()
    }

    fun onRiskLevelToggled(level: Int) {
        _filterState.update {
            val updated = if (level in it.riskLevels) it.riskLevels - level
            else it.riskLevels + level
            it.copy(riskLevels = updated)
        }
        applyFilter()
    }

    fun onAssetClassSelected(assetClass: String?) {
        _filterState.update { it.copy(assetClass = assetClass) }
        applyFilter()
    }

    fun onStrategySelected(strategy: String?) {
        _filterState.update { it.copy(strategy = strategy) }
        applyFilter()
    }

    fun onLikeToggled(ticker: String) {
        viewModelScope.launch {
            // TODO: etfRepository.toggleLike(ticker)
        }
    }

    private fun applyFilter() {
        val current = _uiState.value
        if (current !is UiState.Success) return

        // TODO: filterEtfListUseCase 적용
        // val filtered = filterEtfListUseCase(rawEtfList, _filterState.value)
        //     .map { it.toEtfListItemUiModel() }
        // _uiState.update { UiState.Success(current.data.copy(filteredList = filtered)) }
    }
}

// ─────────────────────────────────────────
// 화면 데이터 모델
// ─────────────────────────────────────────

data class ExploreData(
    val etfList: List<EtfListItemUiModel>,
    val filteredList: List<EtfListItemUiModel>,  // 필터/검색 적용된 결과
    val filter: EtfFilterState                   // 현재 적용된 필터 상태
)

data class EtfListItemUiModel(
    val ticker: String,
    val name: String,
    val currentPrice: Long,
    val changeRate: Double,
    val riskLevel: Int,
    val isLiked: Boolean
)