package com.d102.wye.presentation.simulation.progress.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.d102.wye.presentation.theme.Divider
import com.d102.wye.presentation.theme.EtfRise
import com.d102.wye.presentation.theme.IconInactive
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary

@Composable
fun InvestmentInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Number,
    isCurrencyField: Boolean = false,
    suffix: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )

        Spacer(modifier = Modifier.height(6.dp))

        // 표시용 값
        val displayValue = if (isCurrencyField) {
            value.replace(",", "").toLongOrNull()
                ?.let { "%,d".format(it) }
                ?: value
        } else value

        BasicTextField(
            value = displayValue,
            onValueChange = { input ->
                val cleaned = input
                    .replace(",", "")
                    .filter { it.isDigit() }
                    .trimStart('0')

                val normalized = cleaned.ifEmpty { "0" }

                // 최대 100억
                val maxAmount = 10_000_000

                val finalValue = normalized.toLongOrNull()?.let {
                    if (it > maxAmount) maxAmount.toString() else it.toString()
                } ?: ""

                // "0" 단독 입력 허용하려면 조건 유지
                onValueChange(finalValue)
            },
            textStyle = MaterialTheme.typography.bodySmall.copy(color = TextPrimary),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            cursorBrush = SolidColor(Color.Black),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .border(1.dp, if (isError) EtfRise else Divider, RoundedCornerShape(12.dp))
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodySmall,
                            color = IconInactive
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.weight(1f)) {
                            innerTextField()
                        }
                        if (suffix != null && value.isNotEmpty()) {
                            Text(
                                text = suffix,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isError) EtfRise else PrimaryGreen
                            )
                        }
                    }
                }
            }
        )

        if (isError && !errorMessage.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.labelSmall,
                color = EtfRise,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}