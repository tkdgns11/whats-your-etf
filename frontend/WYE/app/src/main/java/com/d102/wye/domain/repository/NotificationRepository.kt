package com.d102.wye.domain.repository

import com.d102.wye.domain.model.NotificationItem
import kotlinx.coroutines.flow.StateFlow

interface NotificationRepository {
    val items: StateFlow<List<NotificationItem>>
    fun markAsRead(id: Long)
}
