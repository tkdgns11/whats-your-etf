package com.d102.wye.presentation.auth.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onSignupClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,

) {
    // AppScaffold가 이미 innerPadding을 NavHost에 적용하고 있음
    // 여기서 Scaffold를 또 쓰면 패딩 이중 적용 → Column + TopAppBar 조합으로 대신
    Column(modifier = Modifier.fillMaxSize()) {

        TopAppBar(
            title = { Text("로그인") }
            // 로그인 화면은 뒤로가기 없음 → navigationIcon 생략
            // 필요 시: navigationIcon = { IconButton(...) { Icon(...) } }
        )

        // TopBar 아래 콘텐츠 영역 — 전체 패딩 16dp
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp)
        ) {
            // TODO: 실제 UI 구현
            Text("이메일 입력")
            Text("비밀번호 입력")
            Text("로그인 버튼")
        }
    }
}