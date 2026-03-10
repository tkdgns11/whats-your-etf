package com.d102.wye.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.d102.wye.presentation.home.PortfolioSummaryUiModel
import com.d102.wye.presentation.theme.BackGroundLightGreen
import com.d102.wye.presentation.theme.Border
import com.d102.wye.presentation.theme.EtfRise
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.SurfaceWhite
import com.d102.wye.presentation.theme.TextSecondary

@Composable
fun HomePortfolioSummaryCard(
    modifier: Modifier = Modifier,
    portfolio: PortfolioSummaryUiModel?
) {
    if (portfolio == null) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "포트폴리오를 생성하면 수익률을 확인할 수 있습니다.",
                    color = TextSecondary
                )
            }
        }
        return
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(modifier = Modifier.padding(2.dp)) {
            Text(
                text = portfolio.name,
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = portfolio.totalAmount,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "${portfolio.profitRateText} ${portfolio.profitAmountText}",
                color = EtfRise,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 6.dp)
            )
            PortfolioPeriodFilter(modifier = Modifier.padding(top = 14.dp))
            PortfolioChartPlaceholder(modifier = Modifier.padding(top = 10.dp))
        }
    }
}

@Composable
private fun PortfolioPeriodFilter(modifier: Modifier = Modifier) {
    val periods = listOf("일", "주", "월", "년")
    var selectedIndex by remember { mutableIntStateOf(0) }

    Row(
        modifier = modifier
            .background(BackGroundLightGreen, RoundedCornerShape(16.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        periods.forEachIndexed { index, label ->
            Box(
                modifier = Modifier
                    .clickable {
                        selectedIndex = index
                    }
                    .background(
                        color = if (selectedIndex == index) SurfaceWhite else BackGroundLightGreen,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (selectedIndex == index) PrimaryGreen else TextSecondary,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .background(
                            color = androidx.compose.ui.graphics.Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(1.dp)
                )
            }
        }
    }
}

@Composable
private fun PortfolioChartPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(270.dp)
            .background(BackGroundLightGreen, RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(PrimaryGreen, RoundedCornerShape(50))
            )
            Text(
                text = "수익률 차트 영역",
                color = TextSecondary,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "TODO: API/차트 라이브러리 연동",
                color = Border,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
