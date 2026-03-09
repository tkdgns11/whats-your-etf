package com.d102.wye.presentation.home.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d102.wye.domain.model.NotificationCategory
import com.d102.wye.domain.model.NotificationItem
import com.d102.wye.presentation.designsystem.WyeBadge
import com.d102.wye.presentation.designsystem.WyeBadgeStyle
import com.d102.wye.presentation.designsystem.WyeTabs
import com.d102.wye.presentation.designsystem.WyeTopBar
import com.d102.wye.presentation.theme.*

// ── 카테고리 표시 속성 (presentation 전용) ────────────────────────

private data class CategoryStyle(
    val label: String,
    val color: Color,
    val textColor: Color,
)

private fun NotificationCategory.toStyle() = when (this) {
    NotificationCategory.ETF_LISTING   -> CategoryStyle("ETF 상장",  Color(0xFFDCEDD8), Color(0xFF2E6B3E))
    NotificationCategory.MARKET_NOTICE -> CategoryStyle("시장 공지", Color(0xFFF1F5F9), Color(0xFF475569))
    NotificationCategory.DIVIDEND      -> CategoryStyle("분배금",    Color(0xFFFFF3E0), Color(0xFF8A5B00))
    NotificationCategory.PRICE_ALERT   -> CategoryStyle("가격 알림", Color(0xFFFFE9E2), Color(0xFF8B3A2A))
}

// ── 화면 ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onBack: () -> Unit,
    viewModel: NotificationViewModel = hiltViewModel(),
) {
    val tabs = listOf("전체", "미확인")
    var selectedTab by remember { mutableIntStateOf(0) }

    val allItems by viewModel.items.collectAsStateWithLifecycle()
    val displayItems = if (selectedTab == 0) allItems else allItems.filter { !it.isRead }
    val grouped = displayItems.groupBy { it.dateGroup }

    Scaffold(
        containerColor = Background,
        topBar = { WyeTopBar(title = "알림 목록", onBackClick = onBack) },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(top = innerPadding.calculateTopPadding())) {
            WyeTabs(
                titles = tabs,
                selectedIndex = selectedTab,
                onTabSelected = { selectedTab = it },
                containerColor = Background,
            )

            if (displayItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("알림이 없습니다.", color = TextSecondary, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 20.dp),
                ) {
                    grouped.forEach { (dateGroup, items) ->
                        item(key = "header_$dateGroup") {
                            Text(
                                text = dateGroup,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                            )
                        }
                        items(items, key = { it.id }) { item ->
                            NotificationItemRow(
                                item = item,
                                onTap = { viewModel.markAsRead(item.id) }
                            )
                            HorizontalDivider(color = Divider)
                        }
                    }
                }
            }
        }
    }
}

// ── 아이템 ─────────────────────────────────────────────────────

@Composable
private fun NotificationItemRow(item: NotificationItem, onTap: () -> Unit) {
    val style = item.category.toStyle()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (item.isRead) SurfaceCard else Background)
            .clickable(onClick = onTap)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                WyeBadge(
                    label = style.label,
                    color = style.color,
                    textColor = style.textColor,
                    style = WyeBadgeStyle.FILLED,
                )
                Text(
                    text = item.timestamp,
                    fontSize = 12.sp,
                    color = TextSecondary,
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = item.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                lineHeight = 22.sp,
            )
        }
        if (!item.isRead) {
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(PrimaryGreen, shape = androidx.compose.foundation.shape.CircleShape),
            )
        }
    }
}
