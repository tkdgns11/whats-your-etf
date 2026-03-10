package com.d102.wye

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d102.wye.presentation.navigation.AppEntryViewModel
import com.d102.wye.presentation.navigation.AppScaffold
import com.d102.wye.presentation.navigation.Route
import com.d102.wye.presentation.theme.WYETheme
import dagger.hilt.android.AndroidEntryPoint
import android.graphics.Color as AndroidColor

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
//        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                AndroidColor.TRANSPARENT,
                AndroidColor.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                AndroidColor.TRANSPARENT,
                AndroidColor.TRANSPARENT
            )
        )
        setContent {
            WYETheme {
                val appEntryViewModel: AppEntryViewModel = hiltViewModel()
                val isLoggedIn by appEntryViewModel.isLoggedIn.collectAsStateWithLifecycle()
                if (isLoggedIn != null) {
                    val startDestination = if (isLoggedIn == true) Route.Home.route else Route.Login.route
                    AppScaffold(startDestination = startDestination)
                }
            }
        }
    }
}