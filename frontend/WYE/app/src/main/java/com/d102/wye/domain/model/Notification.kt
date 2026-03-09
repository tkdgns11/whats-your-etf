package com.d102.wye.domain.model

data class NotificationItem(
    val id: Long,
    val category: NotificationCategory,
    val timestamp: String,
    val title: String,
    val isRead: Boolean,
    val dateGroup: String,
)

enum class NotificationCategory {
    ETF_LISTING,
    MARKET_NOTICE,
    DIVIDEND,
    PRICE_ALERT,
}
