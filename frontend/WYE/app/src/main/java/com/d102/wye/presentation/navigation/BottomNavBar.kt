package com.d102.wye.presentation.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

/**
 * 앱 하단 네비게이션 바
 *
 * TODO: 팀원 디자인 확정 후 색상/스타일 교체
 *       현재는 Material3 기본 스타일 임시 사용
 *
 * @param currentDestination 현재 활성화된 NavDestination (계층 비교용)
 * @param navController      탭 클릭 시 이동에 사용
 */
@Composable
fun BottomNavBar(
    currentDestination: NavDestination?,
    navController: NavHostController
) {
    NavigationBar(
        tonalElevation = 0.dp,
        // TODO: 팀원 디자인 컬러로 교체
        containerColor = Color.White
    ) {
        BottomNavItem.entries.forEach { item ->
            // 현재 route가 해당 탭의 route 계층에 포함되어 있으면 selected
            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        // 백스택에 Bottom Nav 화면이 쌓이지 않도록
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // 같은 탭 재클릭 시 중복 적재 방지
                        launchSingleTop = true
                        // 이전에 저장된 상태 복원
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(id = item.iconRes),
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(text = item.label)
                },
                // TODO: 팀원 디자인 컬러로 교체
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF1A73E8),
                    selectedTextColor = Color(0xFF1A73E8),
                    unselectedIconColor = Color(0xFF9E9E9E),
                    unselectedTextColor = Color(0xFF9E9E9E),
                    indicatorColor = Color(0xFFE8F0FE)
                )
            )
        }
    }
}