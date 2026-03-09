package com.d102.wye.presentation.mypage.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d102.wye.presentation.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    // TODO: domain/repository/UserRepository 또는 NotificationRepository 주입
    // private val notificationRepository: NotificationRepository,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<UiState<NotificationSettingsUiState>>(UiState.Idle)
    val uiState: StateFlow<UiState<NotificationSettingsUiState>> = _uiState.asStateFlow()

    init {
        loadNotificationSettings()
    }

    fun loadNotificationSettings() {
        viewModelScope.launch {
            _uiState.update { UiState.Loading }

            // TODO: API 연동 시 사용자 알림 설정 조회 결과로 대체
            // TODO: 실패 시 UiState.Error로 매핑
            _uiState.update {
                UiState.Success(
                    NotificationSettingsUiState(
                        appNoticeEnabled = true,
                        etfListingEnabled = false,
                        etfDelistingEnabled = false,
                        portfolioRebalancingEnabled = false,
                        portfolioProfitEnabled = true,
                        newsEnabled = true
                    )
                )
            }
        }
    }

    fun onAppNoticeChanged(enabled: Boolean) = updateSettings {
        copy(appNoticeEnabled = enabled)
    }

    fun onEtfListingChanged(enabled: Boolean) = updateSettings {
        copy(etfListingEnabled = enabled)
    }

    fun onEtfDelistingChanged(enabled: Boolean) = updateSettings {
        copy(etfDelistingEnabled = enabled)
    }

    fun onPortfolioRebalancingChanged(enabled: Boolean) = updateSettings {
        copy(portfolioRebalancingEnabled = enabled)
    }

    fun onPortfolioProfitChanged(enabled: Boolean) = updateSettings {
        copy(portfolioProfitEnabled = enabled)
    }

    fun onNewsChanged(enabled: Boolean) = updateSettings {
        copy(newsEnabled = enabled)
    }

    private fun updateSettings(transform: NotificationSettingsUiState.() -> NotificationSettingsUiState) {
        val current = (_uiState.value as? UiState.Success)?.data ?: return
        val updated = current.transform()

        _uiState.update { UiState.Success(updated) }

        // TODO: API 연동 시 변경된 true/false 값을 서버로 저장
        // TODO: 저장 실패 시 이전 상태 롤백 또는 에러 이벤트 처리
    }
}

data class NotificationSettingsUiState(
    val appNoticeEnabled: Boolean = false,
    val etfListingEnabled: Boolean = false,
    val etfDelistingEnabled: Boolean = false,
    val portfolioRebalancingEnabled: Boolean = false,
    val portfolioProfitEnabled: Boolean = false,
    val newsEnabled: Boolean = false
)
