package com.d102.wye.presentation.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary

@Composable
fun EtfInfoCard(name: String, currentPrice: Long, riskLevel: Int, modifier: Modifier = Modifier) {
    val (badgeBg, badgeFg, badgeLabel) = riskToBadge(riskLevel)

    Column(modifier = modifier) {
        Text(text = name, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(text = "%,d원".format(currentPrice), color = TextSecondary, fontSize = 12.sp)
            CategoryBadge(label = badgeLabel, backgroundColor = badgeBg, textColor = badgeFg)
        }
    }
}