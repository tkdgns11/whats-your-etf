package com.d102.wye.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.d102.wye.presentation.designsystem.SectionHeader
import com.d102.wye.presentation.home.HomeNewsUiModel
import com.d102.wye.presentation.home.PortfolioSummaryUiModel

@Composable
fun HomePortfolioTab(
    portfolios: List<PortfolioSummaryUiModel>,
    newsList: List<HomeNewsUiModel>,
    onNewsClick: (newsId: Long) -> Unit,
    onNewsMoreClick: () -> Unit,
    onPortfolioMoreClick: () -> Unit
) {
    val primaryCardHeight = 400.dp

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionHeader(
                title = "내 포트폴리오",
                onActionClick = onPortfolioMoreClick
            )
        }

        item {
            HomePortfolioSummaryCard(
                modifier = Modifier.height(primaryCardHeight),
                portfolios = portfolios
            )
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
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
