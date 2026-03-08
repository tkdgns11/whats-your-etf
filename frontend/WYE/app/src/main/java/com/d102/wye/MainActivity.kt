package com.d102.wye

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.d102.wye.presentation.navigation.AppScaffold
import com.d102.wye.presentation.navigation.Route
import com.d102.wye.presentation.theme.WYETheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
//        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WYETheme {
                // TODO: 토큰 존재 여부에 따라 startDestination 분기
                // val isLoggedIn by authViewModel.isLoggedIn.collectAsStateWithLifecycle()
                // val startDestination = if (isLoggedIn) Route.Main.route else Route.Login.route

                AppScaffold(
                    startDestination = Route.SimulationEntry.route
                )
            }
        }
    }
}