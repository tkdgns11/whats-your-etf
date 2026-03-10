package com.d102.wye.presentation.designsystem

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.d102.wye.presentation.theme.EtfFall
import com.d102.wye.presentation.theme.EtfNeutral
import com.d102.wye.presentation.theme.EtfRise


@Composable
fun EtfPriceChange(changeRate: Double, changeAmount: Long) {
    val isRise = changeRate > 0
    val changeColor = when {
        changeRate > 0 -> EtfRise
        changeRate < 0 -> EtfFall
        else           -> EtfNeutral
    }
    val arrow = if (isRise) "▲" else "▼"
    val sign = if (isRise) "+" else ""

    Column(horizontalAlignment = Alignment.End) {
        Text(
            text = "$sign${"%.2f".format(changeRate)}%",
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
}