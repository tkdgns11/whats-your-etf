package com.d102.wye.presentation.designsystem

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.TextOnColored

/**
 * Primary 버튼 — 채워진 녹색, 화면 하단 주요 CTA
 *
 * Figma: button/android/large/text/default
 * 기본 full-width. 작게 쓸 땐 modifier로 width 조절.
 */
@Composable
fun WyePrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryGreen,
            contentColor = TextOnColored,
            disabledContainerColor = PrimaryGreen.copy(alpha = 0.4f),
            disabledContentColor = TextOnColored.copy(alpha = 0.6f),
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

/**
 * Outlined 버튼 — 테두리만, 보조 액션/취소
 */
@Composable
fun WyeOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryGreen),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

/**
 * Text 버튼 — "전체보기" 같은 링크성 텍스트 버튼
 */
@Composable
fun WyeTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(contentColor = PrimaryGreen),
        modifier = modifier,
    ) {
        Text(text = text, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

// ── Preview ─────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFF5F0E8)
@Composable
private fun WyeButtonPreview() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        WyePrimaryButton("시뮬레이션 시작", onClick = {})
        WyePrimaryButton("비활성화 버튼", onClick = {}, enabled = false)
        WyeOutlinedButton("나중에 하기", onClick = {})
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WyePrimaryButton("저장", onClick = {}, modifier = Modifier.weight(1f))
            WyeOutlinedButton("취소", onClick = {}, modifier = Modifier.weight(1f))
        }
        WyeTextButton("전체보기", onClick = {})
    }
}
