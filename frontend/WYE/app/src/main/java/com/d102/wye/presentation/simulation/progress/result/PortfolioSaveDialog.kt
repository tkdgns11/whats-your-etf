package com.d102.wye.presentation.simulation.progress.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.d102.wye.presentation.designsystem.WyePrimaryButton
import com.d102.wye.presentation.model.UiState
import com.d102.wye.presentation.theme.Divider
import com.d102.wye.presentation.theme.IconInactive
import com.d102.wye.presentation.theme.PrimaryGreen
import com.d102.wye.presentation.theme.SurfaceVariant
import com.d102.wye.presentation.theme.TextPrimary
import com.d102.wye.presentation.theme.TextSecondary

@Composable
fun PortfolioSaveDialog(
    saveState: UiState<Unit>,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    // 텍스트 필드 입력 상태 관리
    var portfolioName by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. 타이틀
                Text(
                    text = "포트폴리오 저장하기",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 2. 설명 텍스트
                Text(
                    text = "나만의 투자 전략을 식별할 수 있는 이름을 지어주세요.\n저장한 포트폴리오는 나의 전략에서 확인할 수 있습니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 3. 입력 필드 영역 (왼쪽 정렬)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "포트폴리오 명칭",
                        style = MaterialTheme.typography.labelMedium,
                        color = PrimaryGreen
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = portfolioName,
                        onValueChange = { portfolioName = it },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = TextPrimary
                        ),
                        placeholder = {
                            Text(
                                text = "예: 하반기 공격적 성장 전략",
                                style = MaterialTheme.typography.bodyMedium,
                                color = IconInactive
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = Divider,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            cursorColor = TextPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 4. 하단 버튼 2개 (취소 / 저장 완료)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 취소 버튼 (연한 회색 배경)
                    WyePrimaryButton(
                        modifier = Modifier.weight(1f),
                        text = "취소",
                        backgroundColor = SurfaceVariant,
                        textColor = TextSecondary,
                        onClick = onDismiss
                    )

                    // 저장 완료 버튼
                    WyePrimaryButton(
                        modifier = Modifier.weight(1f),
                        text = "저장 완료",
                        onClick = {
                            if (portfolioName.isNotBlank()) {
                                onSave(portfolioName)
                            }
                        }
                    )
                }
            }
        }
    }
}