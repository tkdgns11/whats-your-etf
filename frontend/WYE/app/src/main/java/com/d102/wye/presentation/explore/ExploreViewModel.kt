package com.d102.wye.presentation.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * =====================================================================
 * ViewModel 작성 템플릿
 * =====================================================================
 *
 * 사용법:
 * 1. 이 파일을 복사해서 presentation/[feature]/[FeatureName]ViewModel.kt 에 붙여넣기
 * 2. Explore → 실제 기능 이름으로 전체 교체
 * 3. TODO 주석 위치에 실제 Repository / UseCase 주입 및 로직 구현
 *
 * UiState 설계 기준:
 * - 화면 전체가 로딩/에러로 전환되는 경우 → sealed class (아래 방식)
 * - 여러 독립적인 상태를 동시에 관리할 경우 → data class (ex. SimulationUiState)
 * =====================================================================
 */

// ─────────────────────────────────────────────────────────────────────
// UiState 정의 (같은 파일에 두거나 별도 파일로 분리 가능)
// ─────────────────────────────────────────────────────────────────────

sealed class ExploreUiState {
    object Idle : ExploreUiState()
    object Loading : ExploreUiState()
    data class Success(val data: String) : ExploreUiState()   // TODO: 실제 타입으로 교체
    data class Error(val message: String) : ExploreUiState()
}

// ─────────────────────────────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────────────────────────────

@HiltViewModel
class ExploreViewModel @Inject constructor(
    // TODO: Repository / UseCase 주입
    // private val etfRepository: EtfRepository,
    // private val filterEtfListUseCase: FilterEtfListUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExploreUiState>(ExploreUiState.Idle)
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    init {
        // 화면 진입 시 자동으로 데이터 로드할 경우 여기서 호출
        // loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { ExploreUiState.Loading }

            // TODO: 실제 데이터 로드
            // when (val result = etfRepository.getEtfList()) {
            //     is BaseResult.Success -> _uiState.update { ExploreUiState.Success(result.data) }
            //     is BaseResult.Error   -> _uiState.update { ExploreUiState.Error(result.error.message) }
            // }
        }
    }

    // TODO: 이벤트 핸들러 추가
    // fun onSomethingClick() { ... }
}