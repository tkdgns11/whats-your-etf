package com.d102.wye.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.d102.wye.presentation.designsystem.BottomNavTab
import com.d102.wye.presentation.designsystem.WyeBottomNavBar

private val tabRoutes = mapOf(
    BottomNavTab.HOME       to Route.Home.route,
    BottomNavTab.EXPLORE    to Route.Explore.route,
    BottomNavTab.SIMULATION to Route.Simulation.route,
    BottomNavTab.STRATEGY   to Route.Strategy.route,
    BottomNavTab.MYPAGE     to Route.MyPage.route,
)

private val bottomNavRoutes = tabRoutes.values.toSet()

@Composable
fun AppScaffold(
    startDestination: String = Route.Login.route
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomNavRoutes
    val selectedTab = tabRoutes.entries.firstOrNull { it.value == currentRoute }?.key
        ?: BottomNavTab.HOME

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                WyeBottomNavBar(
                    selectedTab = selectedTab,
                    onTabSelected = { tab ->
                        navController.navigate(tabRoutes[tab]!!) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        AppNavGraph(
            navController = navController,
            contentPadding = innerPadding,
            startDestination = startDestination
        )
    }
}