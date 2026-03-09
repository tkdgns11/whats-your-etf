package com.d102.wye.data.repository

import com.d102.wye.domain.model.NotificationCategory
import com.d102.wye.domain.model.NotificationItem
import com.d102.wye.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor() : NotificationRepository {

    private val _items = MutableStateFlow(mockNotifications())
    override val items: StateFlow<List<NotificationItem>> = _items.asStateFlow()

    override fun markAsRead(id: Long) {
        _items.update { list ->
            list.map { if (it.id == id && !it.isRead) it.copy(isRead = true) else it }
        }
    }
}

private fun mockNotifications() = listOf(
    NotificationItem(1, NotificationCategory.ETF_LISTING,   "2026.03.05 12:54", "2026년 3월 14일 PLUS 코스닥150액티브 ETF 상장 예정", false, "오늘"),
    NotificationItem(2, NotificationCategory.MARKET_NOTICE, "2026.03.05 09:30", "한국거래소, 주요 섹터 지수 정기 변경 안내 (KODEX, TIGER 등)", true, "오늘"),
    NotificationItem(3, NotificationCategory.DIVIDEND,      "2026.03.04 16:15", "ACE 미국배당다우존스 분배금 지급 현황 확인하기", true, "어제"),
    NotificationItem(4, NotificationCategory.ETF_LISTING,   "2026.03.04 11:00", "신규 ETF 'SOL 반도체전공정' 상장 기념 이벤트 안내", true, "어제"),
)
