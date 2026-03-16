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
    const val KEY_IS_LOGGED_IN = "is_logged_in"

    // Database
    const val DATABASE_NAME = "etf_database"
    const val DATABASE_VERSION = 3
}
