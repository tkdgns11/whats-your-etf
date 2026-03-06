package com.d102.wye.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

/**
 * 앱 전체 Scaffold 래퍼
 *
 * 역할:
 * - NavController 생성 및 하위로 전달
 * - 현재 route 감지 → BottomNavBar 표시 여부 결정
 * - Scaffold innerPadding을 AppNavGraph로 전달
 *   → BottomNavBar / TopBar 높이만큼 콘텐츠가 가려지지 않도록 보장
 *
 * BottomNavBar가 보이는 화면: BottomNavItem.routes에 정의된 5개 탭
 * 그 외 화면 (상세, 인증 등): BottomNavBar 없음
 */
@Composable
fun AppScaffold(
    startDestination: String = Route.Login.route
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 현재 route가 Bottom Nav 탭 중 하나일 때만 BottomNavBar 표시
    val showBottomBar = currentRoute in BottomNavItem.routes

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentDestination = navBackStackEntry?.destination,
                    navController = navController
                )
            }
        }
        // topBar는 각 화면 Screen 내부에서 직접 선언
        // 화면마다 TopBar 구성이 다르므로 (타이틀, 버튼 등) 여기서 공통 TopBar 강제하지 않음
    ) { innerPadding ->
        // innerPadding: BottomNavBar 높이 + TopBar 높이를 제외한 실제 콘텐츠 영역
        // AppNavGraph 내부 NavHost에 padding으로 적용됨
        AppNavGraph(
            navController = navController,
            contentPadding = innerPadding,
            startDestination = startDestination
        )
    }
}