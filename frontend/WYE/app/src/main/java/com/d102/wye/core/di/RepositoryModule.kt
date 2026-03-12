package com.d102.wye.core.di

import com.d102.wye.data.repository.AuthRepositoryImpl
import com.d102.wye.data.repository.EtfRepositoryImpl
import com.d102.wye.data.repository.NewsRepositoryImpl
import com.d102.wye.data.repository.NotificationRepositoryImpl
import com.d102.wye.data.repository.SimulationRepositoryImpl
import com.d102.wye.data.repository.StockRepositoryImpl
import com.d102.wye.domain.repository.AuthRepository
import com.d102.wye.domain.repository.EtfRepository
import com.d102.wye.domain.repository.NewsRepository
import com.d102.wye.domain.repository.NotificationRepository
import com.d102.wye.domain.repository.SimulationRepository
import com.d102.wye.domain.repository.StockRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository 인터페이스 ↔ 구현체 연결 모듈
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindEtfRepository(
        impl: EtfRepositoryImpl
    ): EtfRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindNewsRepository(
        impl: NewsRepositoryImpl
    ): NewsRepository

    @Binds
    @Singleton
    abstract fun bindStockRepository(
        impl: StockRepositoryImpl
    ): StockRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        impl: NotificationRepositoryImpl
    ): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindSimulationRepository(
        impl: SimulationRepositoryImpl
    ): SimulationRepository
}