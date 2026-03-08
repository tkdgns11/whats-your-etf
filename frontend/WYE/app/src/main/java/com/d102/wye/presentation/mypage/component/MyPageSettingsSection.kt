package com.d102.wye.presentation.mypage.component

import androidx.compose.foundation.lazy.LazyListScope
import com.d102.wye.presentation.designsystem.WyeListItem

fun LazyListScope.myPageSettingsSection(
    onNotificationSettingClick: () -> Unit,
    onThemeModeClick: () -> Unit
) {
    item { MyPageSectionTitle(title = "설정") }

    item {
        WyeListItem(
            title = "알림 설정",
            showArrow = true,
            showDivider = false,
            onClick = onNotificationSettingClick
        )
    }

    item {
        WyeListItem(
            title = "라이트/다크 모드",
            showArrow = true,
            showDivider = false,
            onClick = onThemeModeClick
        )
    }
}
