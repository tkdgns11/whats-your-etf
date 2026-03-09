package com.d102.wye.presentation.strategy.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.presentation.designsystem.WyeBadge
import com.d102.wye.presentation.strategy.detail.components.RoundedSurface
import com.d102.wye.presentation.theme.Divider
import com.d102.wye.presentation.theme.IconInactive
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.SurfaceVariant
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary
import com.d102.wye.presentation.theme.TextTertiary

@Composable
fun PerformanceSection(data: PerformanceData, isMain: Boolean) {
    RoundedSurface {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            // 뱃지 (최근 1개월 / 과거 1년)
            WyeBadge(
                shape = CircleShape,
                label = data.period,
                textStyle = MaterialTheme.typography.labelSmall,
                color = if (isMain) PrimaryGreen.copy(alpha = 0.1f) else SurfaceVariant,
                textColor = if (isMain) PrimaryGreen else TextTertiary
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "${data.period} 성과",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = data.rate,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 30.sp),
                color = if (data.rate.contains("+")) PrimaryGreen else TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "(${data.dateRange})",
                modifier = Modifier.padding(bottom = 6.dp),
                style = MaterialTheme.typography.labelSmall,
                color = IconInactive
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 차트 영역 TODO: (실제 라이브러리 연결 전 임시 플레이스홀더)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Divider, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                // TODO: 캔버스로 선 그리기 혹은 이미지 배치
//                Icon(
//                    painter = painterResource(id = if (isMain) R.drawable.ic_graph_preview else R.drawable.ic_graph_preview_dashed),
//                    contentDescription = null,
//                    tint = if (isMain) PrimaryGreen else Color.LightGray,
//                    modifier = Modifier.fillMaxSize().padding(10.dp)
//                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = data.dateRange.split("-")[0].trim(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray
                )
                Text(
                    text = data.dateRange.split("-")[1].trim(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray
                )
            }
        }
    }
}