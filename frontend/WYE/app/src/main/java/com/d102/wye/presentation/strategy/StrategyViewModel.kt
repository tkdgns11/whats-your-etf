package com.d102.wye.presentation.strategy

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
class StrategyViewModel @Inject constructor(
    // TODO: Repository 주입
    // private val portfolioRepository: PortfolioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<StrategyListData>>(UiState.Idle)
    val uiState: StateFlow<UiState<StrategyListData>> = _uiState.asStateFlow()

    init {
        loadStrategies()
    }

    fun loadStrategies() {
        viewModelScope.launch {
            _uiState.update { UiState.Loading }
            delay(500)

            // TODO: 실제 API 연동
            // portfolioRepository.getStrategyList().collect { result ->
            //     when (result) {
            //         is BaseResult.Success -> _uiState.update {
            //             UiState.Success(
            //                 StrategyListData(
            //                     realAsset = result.data.realAsset?.toStrategyCardUiModel(),
            //                     strategies = result.data.strategies.map { it.toStrategyCardUiModel() }
            //                 )
            //             )
            //         }
            //         is BaseResult.Error -> _uiState.update { UiState.Error(result.error.message) }
            //     }
            // }

            // Mock - 케이스 교체해서 UI 확인
//            _uiState.update {
//                UiState.Success(
//                    // 케이스 1: 완전 빈 유저 (왼쪽 시안)
//                    StrategyListData()
//
//                    // 케이스 2: 실제 자산 없음 + 전략 있음 (가운데 시안)
//                    // StrategyListData(
//                    //     realAsset = null,
//                    //     strategies = listOf(
//                    //         StrategyCardUiModel("1", "배당왕 안정 추구 전략", "2026.02.15 저장됨", listOf("#SCHD", "#VYM")),
//                    //         StrategyCardUiModel("2", "ESG 사회적 책임 투자", "2026.01.20 저장됨", listOf("#ESGU", "#SUSA"))
//                    //     )
//                    // )
//
//                    // 케이스 3: 실제 자산 있음 + 전략 없음 (오른쪽 시안)
//                    // StrategyListData(
//                    //     realAsset = StrategyCardUiModel("0", "기술주 중심 성장 전략", "MY 전략", listOf("#QQQ", "#VGT"), isRealAsset = true),
//                    //     strategies = emptyList()
//                    // )
//                )
//            }

            // ----------
            // 케이스 2
            // ----------

            // Mock - 케이스 교체해서 UI 확인
            _uiState.update {
                UiState.Success(
                    // 케이스 2: 실제 자산 없음 + 전략 있음 (가운데 시안)
                    StrategyListData(
//                        realAsset = null,
                        realAsset = StrategyCardUiModel(
                            id = "0",
                            title = "기술주 중심 성장 전략",
                            date = "MY 전략",
                            tags = listOf("#QQQ", "#VGT", "#SOXX"),
                            isRealAsset = true
                        ),
                        strategies = listOf(
                            StrategyCardUiModel(
                                id = "1",
                                title = "배당왕 안정 추구 전략",
                                date = "2026.02.15",
                                tags = listOf("#SCHD", "#VYM", "#JEPI")
                            ),
                            StrategyCardUiModel(
                                id = "2",
                                title = "기술주 중심 성장 전략",
                                date = "2026.01.30",
                                tags = listOf("#QQQ", "#VGT", "#SOXX")
                            ),
                            StrategyCardUiModel(
                                id = "3",
                                title = "ESG 사회적 책임 투자",
                                date = "2026.01.20",
                                tags = listOf("#ESGU", "#SUSA")
                            ),
                            StrategyCardUiModel(
                                id = "4",
                                title = "글로벌 분산 투자 전략",
                                date = "2025.12.10",
                                tags = listOf("#VT", "#VXUS", "#EEM")
                            )
                        )
                    )
                )
            }
        }
    }

    fun onDeleteStrategy(strategyId: String) {
        viewModelScope.launch {
            // TODO: portfolioRepository.deleteStrategy(strategyId)
            val current = (_uiState.value as? UiState.Success)?.data ?: return@launch
            _uiState.update {
                UiState.Success(
                    current.copy(
                        strategies = current.strategies.filter { it.id != strategyId }
                    )
                )
            }
        }
    }
}

// ─────────────────────────────────────────
// 화면 데이터 모델
// ─────────────────────────────────────────

data class StrategyCardUiModel(
    val id: String,
    val title: String,
    val date: String,                       // ex: "2026.02.15 저장됨"
    val tags: List<String>,                 // ex: listOf("#SCHD", "#VYM")
    val isRealAsset: Boolean = false        // MY 전략 (실제 자산) 뱃지 여부
)

data class StrategyListData(
    val realAsset: StrategyCardUiModel? = null,             // null = 실제 자산 미연결
    val strategies: List<StrategyCardUiModel> = emptyList() // 저장된 실험 전략 목록
) {
    val isCompletelyEmpty = realAsset == null && strategies.isEmpty()
}