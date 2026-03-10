package com.d102.wye.presentation.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.R
import com.d102.wye.presentation.theme.*
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun EtfListItem(
    name: String,
    ticker: String,
    currentPrice: Long,
    changeRate: Double,
    changeAmount: Long,
    riskLevel: Int,
    isLiked: Boolean,
    onLikeToggled: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (badgeBg, badgeFg, badgeLabel) = riskToBadge(riskLevel)
    val isRise = changeRate > 0
    val changeColor = when {
        changeRate > 0 -> EtfRise
        changeRate < 0 -> EtfFall
        else           -> EtfNeutral
    }
    val arrow = if (isRise) "▲" else "▼"

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            // 로고 (추후 Coil AsyncImage로 교체)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SurfaceVariant),
            ) {
                Text(ticker.take(2), fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))

            // ETF명 + 가격 + 위험등급 뱃지
            Column(modifier = Modifier.weight(1f)) {
                Text(name, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("%,d원".format(currentPrice), color = TextSecondary, fontSize = 12.sp)
                    CategoryBadge(label = badgeLabel, backgroundColor = badgeBg, textColor = badgeFg, isPill = true)
                }
            }

            // 등락률 + 변동금액
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isRise) "+" else ""}${"%.2f".format(changeRate)}%",
                    color = changeColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "$arrow ${"%,d".format(changeAmount)}",
                    color = changeColor,
                    fontSize = 11.sp,
                )
            }
            Spacer(Modifier.width(4.dp))

            // 즐겨찾기 별
            IconButton(onClick = onLikeToggled, modifier = Modifier.size(32.dp)) {
                Icon(
                    painter = painterResource(
                        id = if (isLiked) R.drawable.ic_star else R.drawable.ic_staroutline
                    ),
                    contentDescription = null,
                    tint = if (isLiked) PrimaryGreen else NavInactive,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        HorizontalDivider(color = Divider, modifier = Modifier.padding(horizontal = 16.dp))
    }
}

private fun riskToBadge(level: Int): Triple<Color, Color, String> = when (level) {
    1    -> Triple(BadgeConservative,       BadgeConservativeFont,       "안정형")
    2    -> Triple(BadgeConservativeGrowth, BadgeConservativeGrowthFont, "안정추구형")
    3    -> Triple(BadgeNeutral,            BadgeNeutralFont,            "위험중립형")
    4    -> Triple(BadgeActive,             BadgeActiveFont,             "적극투자형")
    else -> Triple(BadgeAggressive,         BadgeAggressiveFont,         "공격투자형")
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun EtfListItemRisePreview() {
    Column {
        EtfListItem(
            name = "KODEX 200",
            ticker = "KODEX",
            currentPrice = 32_450,
            changeRate = 1.24,
            changeAmount = 400,
            riskLevel = 1,
            isLiked = true,
            onLikeToggled = {},
            onClick = {},
        )
        EtfListItem(
            name = "TIGER 미국나스닥100",
            ticker = "TIGER",
            currentPrice = 104_820,
            changeRate = 2.15,
            changeAmount = 2_210,
            riskLevel = 2,
            isLiked = false,
            onLikeToggled = {},
            onClick = {},
        )
        EtfListItem(
            name = "SOL 미국배당다우존스",
            ticker = "SOL",
            currentPrice = 10_120,
            changeRate = 0.42,
            changeAmount = 45,
            riskLevel = 3,
            isLiked = false,
            onLikeToggled = {},
            onClick = {},
        )
        EtfListItem(
            name = "KODEX 200선물인버스2X",
            ticker = "KODEX",
            currentPrice = 2_145,
            changeRate = -2.31,
            changeAmount = 50,
            riskLevel = 5,
            isLiked = false,
            onLikeToggled = {},
            onClick = {},
        )
    }
}
