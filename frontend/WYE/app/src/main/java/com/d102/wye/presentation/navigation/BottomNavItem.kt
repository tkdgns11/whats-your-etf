package com.d102.wye.presentation.navigation

import androidx.annotation.DrawableRes
import com.d102.wye.R

/**
 * Bottom Navigation Bar 아이템 정의
 *
 * @param route     연결된 Route
 * @param label     탭 하단 레이블
 * @param iconRes   아이콘 리소스 (팀원 디자인 리소스 연결)
 */
enum class BottomNavItem(
    val route: String,
    val label: String,
    @DrawableRes val iconRes: Int
) {
    MAIN(
        route = Route.Home.route,
        label = "홈",
        iconRes = R.drawable.ic_launcher_background         // TODO: 팀원 디자인 리소스로 교체
    ),
    EXPLORE(
        route = Route.Explore.route,
        label = "탐색",
        iconRes = R.drawable.ic_launcher_background
    ),
    SIMULATION(
        route = Route.Simulation.route,
        label = "시뮬레이션",
        iconRes = R.drawable.ic_launcher_background
    ),
    STRATEGY(
        route = Route.Strategy.route,
        label = "나의전략",
        iconRes = R.drawable.ic_launcher_background
    ),
    MY_PAGE(
        route = Route.MyPage.route,
        label = "마이페이지",
        iconRes = R.drawable.ic_launcher_background
    );

    companion object {
        // Bottom Nav에 표시되는 route 집합
        val routes: Set<String> = entries.map { it.route }.toSet()
    }
}