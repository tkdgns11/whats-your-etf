package com.d102.wye.presentation.mypage.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.d102.wye.presentation.designsystem.WyeCard
import com.d102.wye.presentation.designsystem.WyeOutlinedCard
import com.d102.wye.presentation.mypage.MyPageHoldingEtfUiModel
import com.d102.wye.presentation.theme.EtfRise
import com.d102.wye.presentation.theme.TextSecondary

@Composable
fun MyPageHoldingSection(
    holdingEtfs: List<MyPageHoldingEtfUiModel>,
    onHoldingEtfClick: (ticker: String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (holdingEtfs.isEmpty()) {
        WyeOutlinedCard(modifier = modifier.fillMaxWidth()) {
            Text(
                text = "보유하고 있는 ETF가 없습니다.",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "보유 ETF가 생기면 이곳에서 확인할 수 있습니다.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        return
    }

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 0.dp)
    ) {
        items(items = holdingEtfs, key = { it.ticker }) { etf ->
            WyeCard(
                modifier = Modifier
                    .fillParentMaxWidth(0.48f)
                    .height(84.dp),
                onClick = { onHoldingEtfClick(etf.ticker) }
            ) {
                Column(verticalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = etf.name,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1
                    )
                    Text(
                        text = etf.changeRateText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = EtfRise
                    )
                }
            }
        }
    }
}
