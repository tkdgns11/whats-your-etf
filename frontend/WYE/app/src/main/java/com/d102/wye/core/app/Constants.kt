package com.d102.wye.core.app

object Constants {

    // API
    const val BASE_URL = "https://j14d102.p.ssafy.io/api/v1/"

    const val CONNECT_TIMEOUT = 30L // seconds
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L

    // DataStore
    const val PREF_NAME = "wye_prefs"
    const val KEY_ACCESS_TOKEN = "access_token"
    const val KEY_REFRESH_TOKEN = "refresh_token"
    const val KEY_USER_ID = "user_id"
    const val KEY_AUTH_PROVIDER = "auth_provider"
    const val KEY_USER_NICKNAME = "user_nickname"
    const val KEY_KAKAO_USER_ID = "kakao_user_id"
    const val KEY_IS_LOGGED_IN = "is_logged_in"

    // Database
    const val DATABASE_NAME = "wye_db"
    const val DATABASE_VERSION = 1

    // Error Messages
    const val ERROR_NETWORK = "네트워크 연결을 확인해주세요"
    const val ERROR_SESSION_EXPIRED = "세션이 만료되었습니다. 다시 로그인해주세요"
}
