package com.d102.wye.presentation.simulation.entry

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.domain.model.EtfBundle
import com.d102.wye.presentation.designsystem.WyeBadge
import com.d102.wye.presentation.designsystem.WyeOutlinedButton
import com.d102.wye.presentation.designsystem.WyeOutlinedCard
import com.d102.wye.presentation.theme.IconBackGroundOrange
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.SurfaceVariant
import com.d102.wye.presentation.theme.TextTertiary


@Composable
fun BundleCard(bundle: EtfBundle, onClick: () -> Unit) {
    WyeOutlinedCard(
        modifier = Modifier.width(230.dp)
    ) {
        // 이모지 아이콘
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(IconBackGroundOrange, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "😀", fontSize = 24.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 제목
        Text(
            text = bundle.name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        // 설명
        Text(
            text = bundle.summary,
            fontSize = 13.sp,
            color = TextTertiary,
            maxLines = 2
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 태그 리스트
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            bundle.tags.forEach { tag ->
                WyeBadge(
                    label = "#$tag",
                    textStyle = MaterialTheme.typography.labelSmall,
                    color = SurfaceVariant,
                    textColor = TextTertiary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        WyeOutlinedButton(
            text = "상세보기",
            style = MaterialTheme.typography.labelMedium,
            verticalPaddingValues = 4.dp,
            borderColor = PrimaryGreen.copy(alpha = 0.3f),
            onClick = onClick
        )
    }
}