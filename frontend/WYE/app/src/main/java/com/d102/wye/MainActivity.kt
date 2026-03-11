package com.d102.wye

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d102.wye.domain.repository.AuthRepository
import com.d102.wye.presentation.navigation.AppEntryViewModel
import com.d102.wye.presentation.navigation.AppScaffold
import com.d102.wye.presentation.navigation.Route
import com.d102.wye.presentation.theme.WYETheme
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.graphics.Color as AndroidColor

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* 결과 무관하게 토큰 등록 시도 */ }

    override fun onCreate(savedInstanceState: Bundle?) {
//        installSplashScreen()

        super.onCreate(savedInstanceState)

        // 알림 권한 요청 (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // FCM 토큰 가져와서 서버 등록
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            CoroutineScope(Dispatchers.IO).launch {
                authRepository.registerFcmToken(token)
            }
        }

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
                    val startDestination = if (isLoggedIn == true) Route.Home.route else Route.Home.route
                    AppScaffold(startDestination = startDestination)
                }
            }
        }
    }
}