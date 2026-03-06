package com.d102.wye.presentation.designsystem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.TextPrimary

/**
 * 섹션 헤더
 *
 * 홈 화면의 "실시간 ETF 뉴스 | 전체보기" 처럼
 * 섹션 타이틀과 선택적 "전체보기" 링크를 가로로 배치합니다.
 *
 * @param title         섹션 제목
 * @param actionLabel   우측 링크 텍스트 (null 이면 표시 안 함)
 * @param onActionClick 링크 클릭 콜백
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = "전체보기",
    onActionClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )

        if (actionLabel != null && onActionClick != null) {
            Text(
                text = actionLabel,
                color = PrimaryGreen,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable(onClick = onActionClick),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F0E8)
@Composable
private fun SectionHeaderPreview() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 1) 기본 — 제목 + 전체보기
        SectionHeader(title = "실시간 ETF 뉴스", onActionClick = {})

        // 2) 액션 없음 — 제목만
        SectionHeader(title = "거래량 TOP 10", actionLabel = null)

        // 3) 커스텀 액션 레이블
        SectionHeader(title = "나의 전략", actionLabel = "더보기", onActionClick = {})

        // 4) 긴 제목
        SectionHeader(title = "테마별 ETF 추천 포트폴리오", onActionClick = {})
    }
}
