package com.d102.wye.core.di

import android.content.Context
import androidx.room.Room
import com.d102.wye.data.local.dao.LikedEtfDao
import com.d102.wye.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Room Database DI 모듈
 *
 * 제공 대상:
 * - AppDatabase (싱글톤)
 * - 각 DAO (AppDatabase에서 추출)
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val DATABASE_NAME = "etf_database"

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            DATABASE_NAME
        )
            // 마이그레이션 전략 미정의 시 DB를 새로 생성 (개발 중에만 사용, 배포 시 Migration 작성)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideLikedEtfDao(appDatabase: AppDatabase): LikedEtfDao {
        return appDatabase.likedEtfDao()
    }

    // DAO 추가 시 아래에 동일한 패턴으로 추가
    // @Provides
    // @Singleton
    // fun provideXxxDao(appDatabase: AppDatabase): XxxDao {
    //     return appDatabase.xxxDao()
    // }
}