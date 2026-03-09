package com.d102.wye.presentation.strategy.list.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.R
import com.d102.wye.presentation.designsystem.WyeBadge
import com.d102.wye.presentation.strategy.list.StrategyCardUiModel
import com.d102.wye.presentation.theme.Divider
import com.d102.wye.presentation.theme.IconInactive
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.SurfaceVariant
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextTertiary

@Composable
fun StrategyCard(
    strategy: StrategyCardUiModel,
    onItemClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onItemClick(strategy.id.toLong()) },
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(
            width = 1.dp,
            color = if (strategy.isRealAsset) PrimaryGreen else Divider
        ),
        shadowElevation = if (strategy.isRealAsset) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // 1. 배지 (실제 자산일 때만 노출)
                if (strategy.isRealAsset) {
                    WyeBadge(
                        shape = RoundedCornerShape(8.dp),
                        label = "MY 전략",
                        textStyle = MaterialTheme.typography.labelSmall,
                        color = PrimaryGreen,
                        textColor = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // 2. 제목
                Text(
                    text = strategy.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                    color = TextPrimary
                )

                // 3. 날짜
                if (!strategy.isRealAsset) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${strategy.date} 저장됨",
                        style = MaterialTheme.typography.labelMedium,
                        color = IconInactive
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4. 태그 리스트 (#QQQ #VGT 등)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    strategy.tags.take(3).forEach { tag ->
                        WyeBadge(
                            shape = CircleShape,
                            label = "#$tag",
                            textStyle = MaterialTheme.typography.labelSmall,
                            color = SurfaceVariant,
                            textColor = TextTertiary
                        )
                    }
                }
            }

            // 5. 우측 그래프 미리보기 영역
            Image(
                painter = painterResource(id = R.drawable.img_graph_preview), // 굴곡진 선 이미지 추천
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}