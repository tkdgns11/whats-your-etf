package com.d102.wye.presentation.simulation.progress.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d102.wye.R
import com.d102.wye.presentation.designsystem.DashedContainer
import com.d102.wye.presentation.designsystem.WyeCircleIcon
import com.d102.wye.presentation.simulation.progress.PortfolioItem
import com.d102.wye.presentation.simulation.progress.SimulationFormState
import com.d102.wye.presentation.theme.BackGroundLightGreen2
import com.d102.wye.presentation.theme.Border
import com.d102.wye.presentation.theme.IconInactive
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.SurfaceVariant
import com.d102.wye.presentation.theme.SurfaceWhite
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary

@Composable
fun PortfolioSection(
    formState: SimulationFormState,
    onAddClick: () -> Unit,
    onRemoveClick: (String) -> Unit,
    onWeightChange: (String, Int) -> Unit
) {
    Column(
        modifier = Modifier
            .background(BackGroundLightGreen2)
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        // ── 상단 헤더 (타이틀 및 합계 뱃지)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "포트폴리오 구성", style = MaterialTheme.typography.titleSmall)

            val totalWeight = formState.portfolioItems.sumOf { it.weight }
            Box(
                modifier = Modifier.background(
                    color = PrimaryGreen,
                    shape = RoundedCornerShape(12.dp)
                ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "합계: $totalWeight%",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .requiredWidthIn(min = 78.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── 리스트 렌더링 영역 ──
        if (formState.portfolioItems.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                formState.portfolioItems.forEach { item ->
                    PortfolioSliderItemRow(
                        item = item,
                        onWeightChange = { newWeight -> onWeightChange(item.ticker, newWeight) },
                        onRemove = { onRemoveClick(item.ticker) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (formState.portfolioItems.isEmpty()) {
            DashedContainer(
                modifier = Modifier.clickable { onAddClick() },
                strokeWidth = 2.dp,
                borderColor = Border
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_plus),
                        contentDescription = null,
                        tint = IconInactive
                    )
                    Text(
                        text = "ETF 종목 추가하기",
                        style = MaterialTheme.typography.titleSmall,
                        color = IconInactive
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

/**
 * 💡 개별 포트폴리오 아이템 행
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PortfolioSliderItemRow(
    item: PortfolioItem,
    onWeightChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    // 입력창의 텍스트를 관리하기 위한 상태 변수
    var weightTextFieldValue by remember(item.weight) {
        mutableStateOf(TextFieldValue(text = item.weight.toString(), selection = TextRange(item.weight.toString().length)))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceWhite)
            .padding(vertical = 12.dp, horizontal = 16.dp),
    ) {
        // ── 상단 정보 행 (로고, 이름, 퍼센트 입력창, 삭제 버튼) ──
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 좌측 로고 유지
            WyeCircleIcon(
                tag = item.ticker,
                count = 2,
                size = 36.dp,
                backgroundColor = SurfaceVariant,
                contentColor = TextSecondary
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 중앙 텍스트 (종목명 & 티커) 유지
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    color = TextPrimary,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )
                Text(
                    text = item.ticker,
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // ── 우측 퍼센트 직접 입력창 영역 ──
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // 직접 입력을 위한 custom TextField (테두리 없고 크고 굵은 텍스트)
                BasicTextField(
                    value = weightTextFieldValue,
                    onValueChange = { newValue ->
                        // 숫자만 입력받도록 로직 추가
                        val filteredText = newValue.text.filter { it.isDigit() }
                        if (filteredText.isNotEmpty()) {
                            val newWeight = filteredText.toInt().coerceIn(0, 100)
                            // 텍스트 필드 값 업데이트 (커서 위치 유지)
                            weightTextFieldValue = newValue.copy(text = newWeight.toString(), selection = TextRange(newWeight.toString().length))
                            // 뷰모델 콜백 호출
                            onWeightChange(newWeight)
                        } else {
                            // 빈 값일 때 0%로 처리하거나 이전 값 유지 등 UX 결정 필요. 여기선 0%로 처리 예시
                            weightTextFieldValue = newValue.copy(text = "", selection = TextRange(0))
                            onWeightChange(0)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = TextStyle(
                        color = PrimaryGreen,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End
                    ),
                    modifier = Modifier.widthIn(min = 28.dp) // 숫자 너비에 맞춰 유동적으로 변함
                )
                // 단위 텍스트 `%` 추가
                Text(
                    text = "%",
                    color = PrimaryGreen,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // ── 삭제 버튼 (`X`) 유지 ──
            IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = "삭제", tint = TextSecondary, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── 하단 비율 조절 슬라이더 영역 ──
        Slider(
            value = item.weight.toFloat(),
            onValueChange = { newValue ->
                // 💡 슬라이더 값이 바뀌면 텍스트 필드 값도 업데이트 (LaunchedEffect로 처리해도 좋음)
                weightTextFieldValue = TextFieldValue(text = newValue.toInt().toString(), selection = TextRange(newValue.toInt().toString().length))
                // 뷰모델 콜백 호출
                onWeightChange(newValue.toInt())
            },
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(
                thumbColor = PrimaryGreen, // 조절 버튼 색상
                activeTrackColor = PrimaryGreen, // 진행된 부분 선 색상
                inactiveTrackColor = SurfaceVariant // 안 진행된 부분 선 색상
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp) // 슬라이더 양쪽 패딩
        )
    }
}