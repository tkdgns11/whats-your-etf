package com.d102.wye.presentation.home.news

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.d102.wye.R
import com.d102.wye.presentation.designsystem.WyeTopBar
import com.d102.wye.presentation.theme.*

// ── 상세 데이터 모델 ────────────────────────────────────────────

data class NewsDetailUiModel(
    val id: Long,
    val title: String,
    val date: String,
    val thumbnailUrl: String? = null,
    val tags: List<String>,
    val aiSummary: List<String>,
    val body: String,
    val sourceUrl: String,
    val source: String,
    val relatedEtfs: List<RelatedEtfUiModel>,
)

data class RelatedEtfUiModel(
    val ticker: String,
    val name: String,
    val manager: String,
    val changeRate: Double,
    val thumbnailUrl: String? = null,
)

// ── 뉴스 상세 화면 ──────────────────────────────────────────────

@Composable
fun NewsDetailScreen(
    newsId: Long,
    onBack: () -> Unit,
    onEtfClick: (String) -> Unit = {},
) {
    val news = remember(newsId) { mockNewsDetail(newsId) }

    Scaffold(
        containerColor = Background,
        topBar = { WyeTopBar(title = "뉴스 상세보기", onBackClick = onBack) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .verticalScroll(rememberScrollState()),
        ) {
            // 썸네일 + 제목 오버레이
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(PrimaryGreenDark),
            ) {
                if (news.thumbnailUrl != null) {
                    AsyncImage(
                        model = news.thumbnailUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PrimaryGreenDark.copy(alpha = 0.55f)),
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                ) {
                    Text(
                        text = news.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 28.sp,
                        ),
                        color = TextOnColored,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = news.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextOnColored.copy(alpha = 0.8f),
                    )
                }
            }

            // 태그
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                news.tags.forEach { tag ->
                    Text(
                        text = "#$tag",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                        color = PrimaryGreen,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(BackGroundLightGreen)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }

            // AI 핵심 요약
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BackGroundLightGreen2)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_shining),
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = "AI 핵심 요약",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = PrimaryGreen,
                    )
                }
                news.aiSummary.forEach { bullet ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = TextPrimary,
                        )
                        Text(
                            text = bullet,
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 21.sp),
                            color = TextPrimary,
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // 본문
            Text(
                text = news.body,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    lineHeight = 24.sp,
                ),
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            Spacer(Modifier.height(24.dp))

            // 원문 전체 보러가기
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BackGroundLightGreen2)
                    .clickable { }
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "원문 전체 보러가기",
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp),
                    color = TextPrimary,
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp),
                )
            }
            Text(
                text = "출처: ${news.source}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                textAlign = TextAlign.Center,
            )

            // 관련 ETF 추천
            if (news.relatedEtfs.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackGroundLightGreen2)
                        .padding(vertical = 16.dp),
                ) {
                    Text(
                        text = "관련 ETF 추천",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(news.relatedEtfs, key = { it.ticker }) { etf ->
                            RelatedEtfCard(etf = etf, onClick = { onEtfClick(etf.ticker) })
                        }
                    }
                }
            }
        }
    }
}

// ── 관련 ETF 카드 ───────────────────────────────────────────────

@Composable
private fun RelatedEtfCard(etf: RelatedEtfUiModel, onClick: () -> Unit) {
    val rateColor = when {
        etf.changeRate > 0 -> EtfRise
        etf.changeRate < 0 -> EtfFall
        else -> EtfNeutral
    }
    val rateText = if (etf.changeRate > 0) "+${etf.changeRate}%" else "${etf.changeRate}%"

    Column(
        modifier = Modifier
            .width(150.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceWhite)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // 원형 아이콘
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(BackGroundLightGreen),
            contentAlignment = Alignment.Center,
        ) {
            if (etf.thumbnailUrl != null) {
                AsyncImage(
                    model = etf.thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                )
            } else {
                Text(
                    text = etf.ticker.take(4),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = PrimaryGreen,
                )
            }
        }
        // ETF 명
        Text(
            text = etf.name,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            ),
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        // 운용사 + 등락율
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = etf.manager,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = rateText,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = rateColor,
            )
        }
    }
}

// ── 목업 ───────────────────────────────────────────────────────

private fun mockNewsDetail(id: Long) = NewsDetailUiModel(
    id = id,
    title = "미국 연준 금리 동결 발표, 나스닥 기술주 일제히 반등",
    date = "2026.03.04",
    tags = listOf("금리동결", "나스닥", "빅테크", "반도체"),
    aiSummary = listOf(
        "미국 연방준비제도(Fed)가 기준금리를 현 수준에서 동결하기로 만장일치 결정했습니다.",
        "파월 의장은 \"인플레이션 둔화세가 뚜렷해질 때까지 신중을 기할 것\"이라고 강조했습니다.",
        "금리 인상 사이클 종료 기대감에 나스닥 등 주요 기술주들이 강력한 매수세를 보였습니다.",
    ),
    body = """미국 중앙은행인 연방준비제도(Fed·연준)가 4일(현지시간) 기준금리를 현재 수준인 5.25~5.50%로 유지하기로 결정했습니다. 이는 시장의 예상과 일치하는 결과로, 고금리 기조가 정점에 달했다는 인식이 확산되며 뉴욕 증시는 일제히 상승세로 돌아섰습니다.

제롬 파월 연준 의장은 기자회견에서 "인플레이션이 목표치인 2%를 향해 지속 가능한 방식으로 둔화하고 있다는 확신이 더 필요하다"면서도, 추가 금리 인상 가능성에 대해서는 신중한 입장을 보였습니다. 특히 하반기 경제 지표에 따라 금리 인하 여부를 논의할 수 있다는 신호를 보내며 기술주들의 강한 매수세를 이끌어냈습니다.""",
    sourceUrl = "https://example.com/news/1001",
    source = "매일경제",
    relatedEtfs = listOf(
        RelatedEtfUiModel("QQQ", "Nasdaq 100", "Invesco", 1.24),
        RelatedEtfUiModel("VGT", "Tech ETF", "Vanguard", 1.56),
        RelatedEtfUiModel("SOXX", "Semi ETF", "iShares", 0.88),
    ),
)
