package com.d102.wye.presentation.home.notification

import androidx.lifecycle.ViewModel
import com.d102.wye.domain.model.NotificationItem
import com.d102.wye.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repository: NotificationRepository,
) : ViewModel() {

    val items: StateFlow<List<NotificationItem>> = repository.items

    fun markAsRead(id: Long) = repository.markAsRead(id)
}
