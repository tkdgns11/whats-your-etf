package com.d102.wye.presentation.simulation.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d102.wye.presentation.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SimulationViewModel @Inject constructor(
    // TODO: Repository 주입
    // private val simulationRepository: SimulationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<SimulationEntryData>>(UiState.Idle)
    val uiState: StateFlow<UiState<SimulationEntryData>> = _uiState.asStateFlow()

    // 선택된 꾸러미 (상세 다이얼로그 표시용)
    private val _selectedBundle = MutableStateFlow<EtfBundleUiModel?>(null)
    val selectedBundle: StateFlow<EtfBundleUiModel?> = _selectedBundle.asStateFlow()

    init {
        loadBundles()
    }

    fun loadBundles() {
        viewModelScope.launch {
            _uiState.update { UiState.Loading }

            // TODO: 사전 구성 꾸러미 로드
            // when (val result = simulationRepository.getBundles()) {
            //     is BaseResult.Success -> _uiState.update {
            //         UiState.Success(
            //             SimulationEntryData(bundles = result.data.map { it.toUiModel() })
            //         )
            //     }
            //     is BaseResult.Error -> _uiState.update { UiState.Error(result.error.message) }
            // }
        }
    }

    fun onBundleClick(bundle: EtfBundleUiModel) {
        _selectedBundle.update { bundle }
    }

    fun onBundleDialogDismiss() {
        _selectedBundle.update { null }
    }
}

// ─────────────────────────────────────────
// 화면 데이터 모델
// ─────────────────────────────────────────

data class SimulationEntryData(
    val bundles: List<EtfBundleUiModel>  // 사전 구성 꾸러미 목록
)

data class EtfBundleUiModel(
    val id: Int,
    val name: String,                    // 예: "안정형 포트폴리오"
    val description: String,
    val etfTickers: List<String>,        // 포함된 ETF 티커 목록
    val expectedReturnRate: Double       // 예상 수익률
)