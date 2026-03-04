package com.d102.wye.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

/**
 * 앱 전체 NavGraph
 *
 * @param navController  네비게이션 컨트롤러
 * @param contentPadding Scaffold에서 내려오는 innerPadding
 *                       BottomNavBar + TopBar 높이를 제외한 실제 콘텐츠 영역
 * @param startDestination 앱 시작 화면 (로그인 여부에 따라 외부에서 결정)
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    contentPadding: PaddingValues,
    startDestination: String = Route.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)    // BottomNavBar / TopBar 영역 제외한 패딩 적용
    ) {

        // ─────────────────────────────────────────
        // Auth
        // ─────────────────────────────────────────

        composable(Route.Login.route) {
//            LoginScreen(navController = navController)
        }

        composable(Route.Signup.route) {
//            SignupScreen(navController = navController)
        }

        composable(Route.PasswordReset.route) {
//            PasswordResetScreen(navController = navController)
        }

        // ─────────────────────────────────────────
        // Bottom Nav 화면
        // ─────────────────────────────────────────

        composable(Route.Main.route) {
//            MainScreen(navController = navController)
        }


        // ─────────────────────────────────────────
        // ETF 상세 (ticker 파라미터)
        // ─────────────────────────────────────────

        composable(
            route = Route.EtfDetail.ROUTE_PATTERN,
            arguments = listOf(
                navArgument(Route.EtfDetail.ARG_TICKER) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val ticker = backStackEntry.arguments?.getString(Route.EtfDetail.ARG_TICKER) ?: return@composable
//            EtfDetailScreen(
//                ticker = ticker,
//                navController = navController
//            )
        }

        // ─────────────────────────────────────────
        // 뉴스 상세 (newsId 파라미터)
        // ─────────────────────────────────────────

        composable(
            route = Route.NewsDetail.ROUTE_PATTERN,
            arguments = listOf(
                navArgument(Route.NewsDetail.ARG_NEWS_ID) { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val newsId = backStackEntry.arguments?.getLong(Route.NewsDetail.ARG_NEWS_ID) ?: return@composable
//            NewsDetailScreen(
//                newsId = newsId,
//                navController = navController
//            )
        }

        // ─────────────────────────────────────────
        // 시뮬레이션 서브 화면
        // ─────────────────────────────────────────

        composable(Route.SimulationSetup.route) {
//            SimulationSetupScreen(navController = navController)
        }


        // ─────────────────────────────────────────
        // 나의전략 서브 화면 (strategyId 파라미터)
        // ─────────────────────────────────────────

        composable(
            route = Route.StrategyDetail.ROUTE_PATTERN,
            arguments = listOf(
                navArgument(Route.StrategyDetail.ARG_STRATEGY_ID) { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val strategyId = backStackEntry.arguments?.getLong(Route.StrategyDetail.ARG_STRATEGY_ID) ?: return@composable
//            StrategyDetailScreen(
//                strategyId = strategyId,
//                navController = navController
//            )
        }


        // ─────────────────────────────────────────
        // 알림
        // ─────────────────────────────────────────

        composable(Route.Notification.route) {
//            NotificationScreen(navController = navController)
        }
    }
}