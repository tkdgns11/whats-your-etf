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
import com.d102.wye.presentation.auth.login.LoginScreen
import com.d102.wye.presentation.explore.ExploreScreen
import com.d102.wye.presentation.home.HomeScreen
import com.d102.wye.presentation.mypage.MyPageScreen
import com.d102.wye.presentation.simulation.SimulationScreen
import com.d102.wye.presentation.strategy.StrategyScreen

/**
 * 앱 전체 NavGraph
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
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Route.Home.route) {
                        popUpTo(Route.Login.route) { inclusive = true }
                    }
                },
                onSignupClick = { navController.navigate(Route.Signup.route) },
                onForgotPasswordClick = { navController.navigate(Route.PasswordReset.route) }
            )
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

        composable(Route.Home.route) {
            HomeScreen(
                onNewsClick = { newsId -> navController.navigate(Route.NewsDetail(newsId).route) },
                onEtfClick = { ticker -> navController.navigate(Route.EtfDetail(ticker).route) }
            )
        }

        composable(Route.Explore.route) {
            ExploreScreen(
                onEtfClick = { ticker -> navController.navigate(Route.EtfDetail(ticker).route) }
            )
        }

        composable(Route.Simulation.route) {
            SimulationScreen(
                onStartClick = { navController.navigate(Route.SimulationSetup.route) },
                onBundleClick = { }
            )
        }

        composable(Route.Strategy.route) {
            StrategyScreen(
                onStrategyClick = { id -> navController.navigate(Route.StrategyDetail(id).route) },
                onCompareClick = { navController.navigate(Route.StrategyCompare.route) }
            )
        }

        composable(Route.MyPage.route) {
            MyPageScreen(
                onLikedEtfClick = { ticker -> navController.navigate(Route.EtfDetail(ticker).route) },
                onLogoutClick = {
                    navController.navigate(Route.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
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
            val ticker =
                backStackEntry.arguments?.getString(Route.EtfDetail.ARG_TICKER) ?: return@composable
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
            val newsId =
                backStackEntry.arguments?.getLong(Route.NewsDetail.ARG_NEWS_ID) ?: return@composable
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
            val strategyId = backStackEntry.arguments?.getLong(Route.StrategyDetail.ARG_STRATEGY_ID)
                ?: return@composable
//            StrategyDetailScreen(
//                strategyId = strategyId,
//                navController = navController
//            )
        }

    }
}