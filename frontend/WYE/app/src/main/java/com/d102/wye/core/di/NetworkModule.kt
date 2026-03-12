package com.d102.wye.core.di

import com.d102.wye.core.app.Constants
import com.d102.wye.core.network.AuthTokenInterceptor
import com.d102.wye.core.network.TokenRefreshInterceptor
import com.d102.wye.data.remote.api.AlertApiService
import com.d102.wye.data.remote.api.AuthApiService
import com.d102.wye.data.remote.api.EtfApiService
import com.d102.wye.data.remote.api.NewsApiService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    /**
     * OkHttpClient 제공
     *
     * AuthTokenInterceptor는 DataStore를 주입받아 자동 생성됨
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authTokenInterceptor: AuthTokenInterceptor,  // Hilt가 자동 주입
        tokenRefreshInterceptor: TokenRefreshInterceptor  // Hilt가 자동 주입
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(Constants.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authTokenInterceptor)  // JWT Token 자동 추가
            .addInterceptor(tokenRefreshInterceptor)  // JWT Token 자동 추가
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    /** ETF 관련 Retrofit API 인터페이스 구현체를 제공한다. */
    @Provides
    @Singleton
    fun provideEtfApiService(retrofit: Retrofit): EtfApiService {
        return retrofit.create(EtfApiService::class.java)
    }

    /** 인증 관련 Retrofit API 인터페이스 구현체를 제공한다. */
    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    /** 알림 관련 Retrofit API 인터페이스 구현체를 제공한다. */
    @Provides
    @Singleton
    fun provideAlertApiService(retrofit: Retrofit): AlertApiService {
        return retrofit.create(AlertApiService::class.java)
    }

    /** 뉴스 관련 Retrofit API 인터페이스 구현체를 제공한다. */
    @Provides
    @Singleton
    fun provideNewsApiService(retrofit: Retrofit): NewsApiService {
        return retrofit.create(NewsApiService::class.java)
    }
}
