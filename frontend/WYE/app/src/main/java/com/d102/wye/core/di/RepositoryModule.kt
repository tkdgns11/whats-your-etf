package com.d102.wye.core.di

import com.d102.wye.data.repository.EtfRepositoryImpl
import com.d102.wye.domain.repository.EtfRepository
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

    /**
     * EtfRepository 인터페이스 요청 시 EtfRepositoryImpl을 주입
     *
     * ViewModel에서:
     *   @Inject constructor(private val etfRepository: EtfRepository)
     * → Hilt가 이 @Binds를 보고 EtfRepositoryImpl을 자동으로 주입해줌
     * → ViewModel은 구현체(EtfRepositoryImpl)를 전혀 몰라도 됨
     */
    @Binds
    @Singleton
    abstract fun bindEtfRepository(
        impl: EtfRepositoryImpl
    ): EtfRepository
}