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
import com.d102.wye.presentation.auth.join.JoinScreen
import com.d102.wye.presentation.auth.login.LoginScreen
import com.d102.wye.presentation.auth.passwordreset.PasswordResetScreen
import com.d102.wye.presentation.explore.ExploreScreen
import com.d102.wye.presentation.explore.detail.EtfDetailScreen
import com.d102.wye.presentation.explore.stock.StockDetailScreen
import com.d102.wye.presentation.explore.stock.StockEtfListScreen
import com.d102.wye.presentation.home.HomeScreen
import com.d102.wye.presentation.home.notification.NotificationScreen
import com.d102.wye.presentation.mypage.MyPageScreen
import com.d102.wye.presentation.simulation.entry.SimulationEntryScreen
import com.d102.wye.presentation.simulation.progress.SimulationScreen
import com.d102.wye.presentation.strategy.detail.StrategyDetailScreen
import com.d102.wye.presentation.strategy.list.StrategyScreen

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
                onJoinClick = { navController.navigate(Route.Join.route) },
                onForgotPasswordClick = { navController.navigate(Route.PasswordReset.route) }
            )
        }

        composable(Route.Join.route) {
            JoinScreen(
                onBackClick = { navController.popBackStack() },
                onStartClick = {
                    navController.navigate(Route.Home.route) {
                        popUpTo(Route.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Route.PasswordReset.route) {
            PasswordResetScreen(
                onBackClick = { navController.popBackStack() },
                onCloseClick = { navController.popBackStack() },
                onLoginClick = {
                    navController.navigate(Route.Login.route) {
                        popUpTo(Route.PasswordReset.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // ─────────────────────────────────────────
        // Bottom Nav 화면
        // ─────────────────────────────────────────

        composable(Route.Home.route) {
            HomeScreen(
                onNewsClick = { newsId -> navController.navigate(Route.NewsDetail(newsId).route) },
                onEtfClick = { ticker -> navController.navigate(Route.EtfDetail(ticker).route) },
                onNotificationClick = { navController.navigate(Route.Notification.route) }
            )
        }

        composable(Route.Explore.route) {
            ExploreScreen(
                onEtfClick = { ticker, riskLevel ->
                    navController.navigate(
                        Route.EtfDetail(
                            ticker,
                            riskLevel
                        ).route
                    )
                }
            )
        }

        composable(Route.SimulationEntry.route) {
            SimulationEntryScreen(
                onMakePortfolioClick = { navController.navigate(Route.Simulation.route) },
            )
        }

        composable(Route.Strategy.route) {
            StrategyScreen(
                onStrategyClick = { id -> navController.navigate(Route.StrategyDetail(id).route) },
                onCompareClick = { navController.navigate(Route.StrategyCompare.route) },
                onCreateFirstStrategyClick = { navController.navigate(Route.Simulation.route) }
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
                navArgument(Route.EtfDetail.ARG_TICKER) { type = NavType.StringType },
                navArgument(Route.EtfDetail.ARG_RISK_LEVEL) {
                    type = NavType.IntType; defaultValue = 0
                },
            )
        ) {
            EtfDetailScreen(
                onBack = { navController.popBackStack() },
                onStockClick = { ticker -> navController.navigate(Route.StockDetail(ticker).route) },
            )
        }

        // ─────────────────────────────────────────
        // 종목 상세 (ticker 파라미터)
        // ─────────────────────────────────────────

        composable(
            route = Route.StockDetail.ROUTE_PATTERN,
            arguments = listOf(
                navArgument(Route.StockDetail.ARG_TICKER) { type = NavType.StringType }
            )
        ) {
            StockDetailScreen(
                onBack = { navController.popBackStack() },
                onEtfListClick = { ticker -> navController.navigate(Route.StockEtfList(ticker).route) },
                onEtfClick = { ticker -> navController.navigate(Route.EtfDetail(ticker).route) },
                onRelatedStockClick = { ticker -> navController.navigate(Route.StockDetail(ticker).route) },
            )
        }

        // 종목에 포함된 ETF 전체 목록
        composable(
            route = Route.StockEtfList.ROUTE_PATTERN,
            arguments = listOf(
                navArgument(Route.StockEtfList.ARG_TICKER) { type = NavType.StringType }
            )
        ) {
            StockEtfListScreen(
                onBack = { navController.popBackStack() },
                onEtfClick = { ticker -> navController.navigate(Route.EtfDetail(ticker).route) },
            )
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
        // 시뮬레이션 진행 화면
        // ─────────────────────────────────────────

        composable(Route.Simulation.route) {
            SimulationScreen(
                onBackClick = { navController.popBackStack() },
                onAddEtfClick = {}
            )
        }

        // ─────────────────────────────────────────
        // 알림 목록
        // ─────────────────────────────────────────

        composable(Route.Notification.route) {
            NotificationScreen(
                onBack = { navController.popBackStack() }
            )
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
            StrategyDetailScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

    }
}
