package com.d102.wye.core.di

import com.d102.wye.domain.usecase.etf.FilterEtfListUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideFilterEtfListUseCase(): FilterEtfListUseCase = FilterEtfListUseCase()
}
