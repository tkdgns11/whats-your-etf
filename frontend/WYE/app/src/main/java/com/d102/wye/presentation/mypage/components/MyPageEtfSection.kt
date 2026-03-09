package com.d102.wye.presentation.mypage.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.d102.wye.presentation.designsystem.SectionHeader
import com.d102.wye.presentation.designsystem.WyeListItem
import com.d102.wye.presentation.mypage.MyPageData

fun LazyListScope.myPageEtfSection(
    data: MyPageData,
    onHoldingEtfMoreClick: () -> Unit,
    onHoldingEtfClick: (ticker: String) -> Unit,
    onLikedEtfListClick: () -> Unit
) {
    item {
        SectionHeader(
            title = "보유 ETF",
            modifier = Modifier.padding(horizontal = 16.dp),
            actionLabel = if (data.holdingEtfs.isEmpty()) null else "전체보기",
            onActionClick = if (data.holdingEtfs.isEmpty()) null else onHoldingEtfMoreClick
        )
    }

    item {
        MyPageHoldingSection(
            holdingEtfs = data.holdingEtfs,
            onHoldingEtfClick = onHoldingEtfClick,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }

    item { MyPageSectionTitle(title = "ETF") }

    item {
        WyeListItem(
            title = "관심있는 ETF",
            trailingText = "${data.likedEtfCount}개",
            showArrow = true,
            showDivider = false,
            onClick = onLikedEtfListClick
        )
    }
}
