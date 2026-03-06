package com.d102.wye.presentation.simulation.entry

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.presentation.theme.IconBackGroundOrange
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.SurfaceVariant
import com.d102.wye.presentation.theme.TextTertiary


@Composable
fun BundleCard(bundle: EtfBundleUiModel, onClick: () -> Unit) {
    // 카드 외곽선 및 배경
    Column(
        modifier = Modifier
            .width(230.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, SurfaceVariant, RoundedCornerShape(12.dp))
            .padding(16.dp)
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
            text = bundle.description,
            fontSize = 13.sp,
            color = TextTertiary,
            maxLines = 2
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 태그 리스트
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            bundle.tags.forEach { tag ->
                Box(
                    modifier = Modifier
                        .background(SurfaceVariant, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "#$tag",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextTertiary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 상세보기 버튼
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth(),
            border = BorderStroke(width = 1.dp, color = PrimaryGreen.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryGreen)
        ) {
            Text(
                modifier = Modifier.padding(vertical = 4.dp),
                text = "상세보기",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}