package com.d102.wye.presentation.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.d102.wye.presentation.theme.*

/**
 * 뉴스 카드
 *
 * 홈 화면 "실시간 ETF 뉴스" 섹션에서 사용하는 카드 컴포넌트.
 *
 * @param category      뱃지에 표시할 카테고리 (예: "위험형 ETF")
 * @param categoryColor 뱃지 배경색 (기본: BadgeConservative)
 * @param title         기사 제목
 * @param thumbnailUrl  썸네일 이미지 URL (없으면 null)
 * @param timeAgo       경과 시간 문자열 (예: "2시간 전")
 * @param source        언론사 이름 (예: "파이낸셜 뉴스")
 * @param onClick       카드 클릭 콜백
 */
@Composable
fun NewsCard(
    category: String,
    title: String,
    timeAgo: String,
    source: String,
    modifier: Modifier = Modifier,
    categoryColor: androidx.compose.ui.graphics.Color = BadgeConservative,
    categoryFontColor: androidx.compose.ui.graphics.Color = BadgeConservativeFont,
    thumbnailUrl: String? = null,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceWhite)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // ── 텍스트 영역 ──────────────────────────────────────────
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // 카테고리 뱃지
            CategoryBadge(label = category, backgroundColor = categoryColor, textColor = categoryFontColor)

            // 기사 제목
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp,
            )

            // 시간 · 출처
            Text(
                text = "$timeAgo · $source",
                color = TextSecondary,
                fontSize = 11.sp,
            )
        }

        // ── 썸네일 ──────────────────────────────────────────────
        if (thumbnailUrl != null) {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(EtfNeutral),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(EtfNeutral),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F0E8)
@Composable
private fun NewsCardPreview() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        NewsCard(
            category = "위험중립형",
            categoryColor = BadgeNeutral,
            categoryFontColor = BadgeNeutralFont,
            title = "나스닥 100 3배 레버리지, 변동성 확대에도 투자자 매수세 집중",
            timeAgo = "2시간 전",
            source = "파이낸셜 뉴스",
        )
        NewsCard(
            category = "적극투자형",
            categoryColor = BadgeActive,
            categoryFontColor = BadgeActiveFont,
            title = "반도체 섹터 ETF, 인공지능 수요 폭증에 수익률 고공행진",
            timeAgo = "5시간 전",
            source = "경제타임즈",
        )
    }
}
