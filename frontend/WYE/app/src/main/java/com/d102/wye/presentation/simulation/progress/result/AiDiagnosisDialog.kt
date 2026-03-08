package com.d102.wye.presentation.simulation.progress.result

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.d102.wye.R
import com.d102.wye.domain.model.AiDiagnosisResult
import com.d102.wye.presentation.designsystem.WyeCard
import com.d102.wye.presentation.designsystem.WyePrimaryButton
import com.d102.wye.presentation.model.UiState
import com.d102.wye.presentation.theme.*

@Composable
fun AiDiagnosisDialog(
    uiState: UiState<AiDiagnosisResult>,
    onDismiss: () -> Unit
) {

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
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. 다이얼로그 공통 타이틀
                Text(
                    text = "AI 포트폴리오 진단",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 2. 상태별 화면 분기
                when (uiState) {
                    is UiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = PrimaryGreen)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "AI가 포트폴리오를 분석 중입니다...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    is UiState.Success -> {
                        AiDiagnosisSuccessContent(data = uiState.data)
                    }

                    is UiState.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "진단 결과를 불러오지 못했습니다.\n다시 시도해주세요.",
                                color = Color.Red,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    else -> Unit
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 3. 닫기 버튼
                WyePrimaryButton(
                    text = "닫기",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onDismiss
                )
            }
        }
    }
}

// 성공했을 때의 내용물 (디자인 시안 부분)
@Composable
private fun AiDiagnosisSuccessContent(data: AiDiagnosisResult) {
    Column(modifier = Modifier.fillMaxWidth()) {

        // 1. 요약 박스
        WyeCard(
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.2f)),
            containerColor = PrimaryGreen.copy(alpha = 0.05f),
            elevation = 0.dp
        ) {
            Row(
                modifier = Modifier.padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_rocket),
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        text = data.mainTitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = PrimaryGreen
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = data.subTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. 태그 리스트
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            data.tags.forEach { tag ->
                Surface(
                    border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(20.dp),
                    color = PrimaryGreen.copy(alpha = 0.1f),
                ) {
                    Text(
                        text = "#$tag",
                        style = MaterialTheme.typography.labelMedium,
                        color = PrimaryGreen,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // 3. 요약 상세 섹션 타이틀
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_ai_diagnosis),
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = "요약 상세",
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4. 상세 내용 텍스트 박스
        WyeCard(
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, Divider),
            elevation = 0.dp
        ) {
            Text(
                modifier = Modifier.padding(2.dp),
                text = data.feedback,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp,
                color = TextDetail,
            )
        }
    }
}