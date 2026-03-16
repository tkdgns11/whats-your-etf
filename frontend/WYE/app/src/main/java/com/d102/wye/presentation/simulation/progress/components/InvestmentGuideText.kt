package com.d102.wye.presentation.simulation.progress.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.d102.wye.domain.state.InvestmentType
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.TextTertiary
import java.text.DecimalFormat

@Composable
fun InvestmentGuideText(
    type: InvestmentType,
    amountStr: String,
    periodStr: String
) {
    // 입력된 문자열을 안전하게 숫자로 변환
    val amount = amountStr.replace(",", "").toLongOrNull() ?: 0L
    val period = periodStr.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0

    // 둘 중 하나라도 입력되지 않았다면 안내 멘트 숨기기
    if (amount == 0L || period == 0) {
        return
    }

    val formatter = DecimalFormat("#,###")
    val formattedAmount = formatter.format(amount)
    val formattedPeriod = formatter.format(period)

    val highlightStyle = SpanStyle(color = PrimaryGreen, fontWeight = FontWeight.SemiBold)
    val defaultColor = TextTertiary

    // 투자 유형에 따른 텍스트 포맷팅
    val annotatedText = buildAnnotatedString {
        if (type == InvestmentType.REGULAR_SAVING) {
            // [적립형] 매월 00원씩 00개월 동안 총 00원을 투자하게 됩니다.
            val totalAmount = formatter.format(amount * period)

            append("매월 ")
            withStyle(highlightStyle) { append("${formattedAmount}원") }
            append("씩 ")
            withStyle(highlightStyle) { append("${formattedPeriod}개월") }
            append(" 동안 총 ")
            withStyle(highlightStyle) { append("${totalAmount}원") }
            append("을 투자하게 됩니다.")

        } else {
            // [거치형] 초기 00원을 00개월 동안 투자하게 됩니다.
            append("초기 ")
            withStyle(highlightStyle) { append("${formattedAmount}원") }
            append("을 ")
            withStyle(highlightStyle) { append("${formattedPeriod}개월") }
            append(" 동안 투자하게 됩니다.")
        }
    }

    Text(
        text = annotatedText,
        style = MaterialTheme.typography.bodyMedium,
        color = defaultColor,
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
    )
}