package com.d102.wye.presentation.home.news

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.d102.wye.presentation.designsystem.WyeTopBar
import com.d102.wye.presentation.theme.*

// ── 데이터 모델 ─────────────────────────────────────────────────

data class NewsUiModel(
    val id: Long,
    val category: String,
    val title: String,
    val summary: String,
    val timeAgo: String,
    val source: String,
    val thumbnailUrl: String? = null,
)

// ── 뉴스 목록 화면 ──────────────────────────────────────────────

@Composable
fun NewsListScreen(
    onBack: () -> Unit,
    onNewsClick: (Long) -> Unit,
) {
    val categories = listOf("전체", "금리/거시경제", "반도체", "AI/테크", "배당", "2차전지", "글로벌")
    var selectedCategory by remember { mutableStateOf("전체") }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    val allNews = remember { mockNewsList() }
    val categoryFiltered = if (selectedCategory == "전체") allNews else allNews.filter { it.category == selectedCategory }
    val displayNews = if (searchQuery.isBlank()) categoryFiltered else categoryFiltered.filter { it.title.contains(searchQuery, ignoreCase = true) }

    val featured = displayNews.firstOrNull()
    val restNews = if (displayNews.size > 1) displayNews.drop(1) else emptyList()

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) focusRequester.requestFocus()
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            AnimatedContent(
                targetState = isSearchActive,
                transitionSpec = {
                    val enter = fadeIn(tween(250, easing = FastOutSlowInEasing)) +
                            scaleIn(initialScale = 0.96f, animationSpec = tween(250, easing = FastOutSlowInEasing))
                    val exit = fadeOut(tween(150)) +
                            scaleOut(targetScale = 0.96f, animationSpec = tween(150))
                    enter togetherWith exit
                },
                label = "search_topbar",
            ) { searchMode ->
                if (searchMode) {
                    // 검색 모드 TopBar
                    Surface(
                        color = SurfaceWhite,
                        shadowElevation = 2.dp,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(onClick = {
                                isSearchActive = false
                                searchQuery = ""
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "검색 닫기",
                                    tint = TextPrimary,
                                )
                            }
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(focusRequester),
                                placeholder = {
                                    Text(
                                        text = "뉴스 검색",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                                        color = TextSecondary,
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = { /* 검색 실행 */ }),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = SurfaceWhite,
                                    unfocusedContainerColor = SurfaceWhite,
                                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                                    cursorColor = PrimaryGreen,
                                ),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 15.sp,
                                    color = TextPrimary,
                                ),
                            )
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "지우기",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // 일반 TopBar
                    WyeTopBar(
                        title = "뉴스",
                        onBackClick = onBack,
                        actions = {
                            IconButton(onClick = { isSearchActive = true }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "검색",
                                    tint = TextPrimary,
                                )
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()),
        ) {
            // 카테고리 칩
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    categories.forEach { cat ->
                        val selected = cat == selectedCategory
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            ),
                            color = if (selected) TextOnColored else TextPrimary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (selected) PrimaryGreen else SurfaceVariant)
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                }
            }

            // 검색 결과 없음
            if (displayNews.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 80.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(40.dp),
                            )
                            Text(
                                text = "검색 결과가 없습니다",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                                color = TextSecondary,
                            )
                        }
                    }
                }
            }

            // 피처드 뉴스 (첫 번째)
            if (featured != null) {
                item(key = "featured_${featured.id}") {
                    FeaturedNewsCard(
                        news = featured,
                        onClick = { onNewsClick(featured.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 20.dp),
                    )
                }
            }

            // 최신 뉴스 헤더
            if (restNews.isNotEmpty()) {
                item {
                    Text(
                        text = if (isSearchActive && searchQuery.isNotBlank()) "검색 결과" else "최신 뉴스",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }
            }

            // 나머지 뉴스 목록
            items(restNews, key = { it.id }) { news ->
                NewsListItem(
                    news = news,
                    onClick = { onNewsClick(news.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Divider,
                )
            }

            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}

// ── 피처드 카드 ─────────────────────────────────────────────────

@Composable
private fun FeaturedNewsCard(
    news: NewsUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(PrimaryGreenDark)
            .clickable(onClick = onClick),
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
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 24.sp,
                ),
                color = TextOnColored,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "${news.source} · ${news.timeAgo}",
                style = MaterialTheme.typography.bodySmall,
                color = TextOnColored.copy(alpha = 0.8f),
            )
        }
    }
}

// ── 목록 아이템 ─────────────────────────────────────────────────

@Composable
private fun NewsListItem(
    news: NewsUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.clickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = news.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 21.sp,
                ),
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = news.summary,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${news.source} · ${news.timeAgo}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
        }
        if (news.thumbnailUrl != null) {
            AsyncImage(
                model = news.thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceVariant),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceVariant),
            )
        }
    }
}

// ── 목업 ───────────────────────────────────────────────────────

private fun mockNewsList() = listOf(
    NewsUiModel(1001, "금리/거시경제", "미국 연준 금리 동결 발표, 나스닥 기술주 일제히 반등", "미국 중앙은행인 연방준비제도(Fed·연준)가 4일 기준금리를 현재 수준인 5.25~5.50%로 유지하기로 결정했습니다.", "1시간 전", "금융경제뉴스"),
    NewsUiModel(1002, "반도체", "반도체 수요 회복세에 관련 ETF 수익률 두 자리수 달성", "글로벌 공급망 안정화와 수요 회복이 맞...", "2시간 전", "매일경제"),
    NewsUiModel(1003, "배당", "배당귀족주의 귀환? 변동성 장세에서 돋보이는 ETF들", "안정적인 현금 흐름을 중시하는 투자자...", "3시간 전", "한국경제"),
    NewsUiModel(1004, "AI/테크", "AI 테마 ETF 열풍, 거품인가 새로운 산업의 서막인가", "생성형 AI 기술의 급격한 발전으로 관련 ...", "5시간 전", "테크인사이드"),
    NewsUiModel(1005, "금리/거시경제", "에너지 패러다임 변화, 친환경 에너지 ETF 장기 전망 우수", "글로벌 탄소 중립 정책 강화에 따라 신재...", "6시간 전", "연합인포맥스"),
)
