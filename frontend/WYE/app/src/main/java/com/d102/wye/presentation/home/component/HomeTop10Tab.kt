package com.d102.wye.presentation.home.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.d102.wye.presentation.designsystem.SectionHeader
import com.d102.wye.presentation.home.HomeNewsUiModel
import com.d102.wye.presentation.home.Top10EtfUiModel

@Composable
fun HomeTop10Tab(
    top10Etfs: List<Top10EtfUiModel>,
    newsList: List<HomeNewsUiModel>,
    onEtfClick: (ticker: String) -> Unit,
    onNewsClick: (newsId: Long) -> Unit,
    onNewsMoreClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionHeader(
                title = "거래량 TOP 10 ETF",
                actionLabel = null
            )
        }

        item {
            HomeTop10HeatmapCard(
                top10Etfs = top10Etfs,
                onEtfClick = onEtfClick
            )
        }

        item {
            SectionHeader(
                title = "실시간 ETF 뉴스",
                onActionClick = onNewsMoreClick
            )
        }

        items(items = newsList, key = { it.id }) { news ->
            HomeNewsListCard(
                news = news,
                onClick = { onNewsClick(news.id) }
            )
        }
    }
}
