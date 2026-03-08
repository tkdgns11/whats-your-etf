package com.d102.wye.presentation.simulation.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d102.wye.domain.model.BundleEtfItem
import com.d102.wye.domain.model.EtfBundle
import com.d102.wye.domain.model.EtfBundleDetail
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
class SimulationEntryViewModel @Inject constructor(
    // TODO: Repository 주입
    // private val simulationRepository: SimulationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<SimulationEntryData>>(UiState.Idle)
    val uiState: StateFlow<UiState<SimulationEntryData>> = _uiState.asStateFlow()

    private val _selectedBundleDetail = MutableStateFlow<EtfBundleDetail?>(null)
    val selectedBundleDetail: StateFlow<EtfBundleDetail?> = _selectedBundleDetail.asStateFlow()

    init {
        loadBundles()
    }

    fun loadBundles() {
        viewModelScope.launch {
            _uiState.update { UiState.Loading }

            // EtfBundleUiModel → EtfBundle로 교체
            val mockData = listOf(
                EtfBundle(
                    id = 1,
                    name = "안전제일 배당왕",
                    summary = "꾸준한 현금흐름을 원한다면",
                    tags = listOf("배당", "저변동성")
                ),
                EtfBundle(
                    id = 2,
                    name = "빅테크 로켓",
                    summary = "나스닥 대장주 위주의 강력한 성장",
                    tags = listOf("기술주")
                ),
                EtfBundle(
                    id = 3,
                    name = "올웨더 방어막",
                    summary = "어떤 경제 위기에도 흔들림 없이",
                    tags = listOf("식품주", "저변동성")
                )
            )

            _uiState.update {
                UiState.Success(SimulationEntryData(bundles = mockData))
            }
        }
    }

    fun onBundleClick(bundle: EtfBundle) {
        viewModelScope.launch {
            // 나중에 API로 상세 조회
            // val detail = repository.getBundleDetail(bundle.id)
            // _selectedBundleDetail.update { detail }

            // 지금은 Mock
            _selectedBundleDetail.update {
                EtfBundleDetail(
                    id = bundle.id,
                    name = bundle.name,
                    description = "하락장에서도 든든하게 계좌를 지켜주는 고배당 ETF 위주의 방어형 포트폴리오입니다.",
                    etfItems = listOf(
                        BundleEtfItem("SCHD", "Schwab US Dividend Equity"),
                        BundleEtfItem("SPY", "S&P 500 ETF Trust"),
                        BundleEtfItem("TLT", "20+ Year Treasury Bond")
                    )
                )
            }
        }
    }

    fun onBundleDialogDismiss() {
        viewModelScope.launch {
            delay(100L)
            _selectedBundleDetail.update { null }
        }
    }
}

data class SimulationEntryData(
    val bundles: List<EtfBundle>        // 목록용
)
