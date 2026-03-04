package com.d102.wye.core.app

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Application 클래스
 */
@HiltAndroidApp
class BaseApplication : Application() {


    companion object {
        private var instance: BaseApplication? = null

        /**
         * Application Context를 반환합니다
         */
        fun getContext(): Context {
            return instance?.applicationContext
                ?: throw IllegalStateException("Application not initialized")
        }

        // Notification Channel IDs
        const val CHANNEL_NEWS_SERVICE = "news_channel"
    }


    override fun onCreate() {
        super.onCreate()
        instance = this

        // Timber 초기화
        Timber.plant(Timber.DebugTree())

        // 카카오 SDK 초기화 (BuildConfig 사용)
//        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
//        Timber.d("Kakao SDK initialized with key: ${BuildConfig.KAKAO_NATIVE_APP_KEY}")
    }
}