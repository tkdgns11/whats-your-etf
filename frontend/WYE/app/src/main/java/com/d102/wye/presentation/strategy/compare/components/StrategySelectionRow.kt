package com.d102.wye.presentation.strategy.compare.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.d102.wye.presentation.strategy.compare.CompareStrategyItem
import com.d102.wye.presentation.theme.Border
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.TextPrimary

@Composable
fun StrategySelectionRow(
    item: CompareStrategyItem,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (item.isSelected) PrimaryGreen else Border,
        label = "borderColor"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .border(width = 1.5.dp, color = borderColor, shape = RoundedCornerShape(6.dp))
                .background(color = Color.White, shape = RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(
                        color = if (item.isSelected) PrimaryGreen else Color.White,
                        shape = RoundedCornerShape(3.dp)
                    )
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = item.name,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = item.returnRate,
            style = MaterialTheme.typography.bodyMedium,
            color = if (item.returnRate.contains("+")) Color(0xFFE56A6A) else Color(0xFF4B89DC)
        )
    }
}