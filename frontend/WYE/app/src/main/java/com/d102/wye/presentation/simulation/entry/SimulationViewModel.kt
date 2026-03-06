package com.d102.wye.presentation.simulation.entry

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
class SimulationViewModel @Inject constructor(
    // TODO: Repository 주입
    // private val simulationRepository: SimulationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<SimulationEntryData>>(UiState.Idle)
    val uiState: StateFlow<UiState<SimulationEntryData>> = _uiState.asStateFlow()

    private val _selectedBundle = MutableStateFlow<EtfBundleUiModel?>(null)
    val selectedBundle: StateFlow<EtfBundleUiModel?> = _selectedBundle.asStateFlow()

    init {
        loadBundles()
    }

    fun loadBundles() {
        viewModelScope.launch {
            // 로딩 상태를 잠깐 보여주기 위한 처리 (실제 통신 느낌 내기)
            _uiState.update { UiState.Loading }
            delay(500) // 0.5초 대기

            // 이미지 UI 기반 목(Mock) 데이터 생성
            val mockData = listOf(
                EtfBundleUiModel(
                    id = 1,
                    name = "안전제일 배당왕",
                    description = "꾸준한 현금흐름을 원한다면",
                    tags = listOf("배당", "저변동성"),
                ),
                EtfBundleUiModel(
                    id = 2,
                    name = "빅테크 로켓",
                    description = "나스닥 대장주 위주의 강력한 성장",
                    tags = listOf("기술주"),
                ),
                EtfBundleUiModel(
                    id = 3,
                    name = "올웨더 방어막",
                    description = "어떤 경제 위기에도 흔들림 없이",
                    tags = listOf("식품주", "저변동성"),
                )
            )

            // 성공 상태로 데이터 방출
            _uiState.update {
                UiState.Success(SimulationEntryData(bundles = mockData))
            }
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
    val name: String,
    val description: String,
    val tags: List<String>
)