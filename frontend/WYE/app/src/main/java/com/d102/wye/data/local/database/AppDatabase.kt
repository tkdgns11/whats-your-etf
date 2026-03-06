package com.d102.wye.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.d102.wye.data.local.dao.LikedEtfDao
import com.d102.wye.data.local.entity.LikedEtfEntity

/**
 * Room Database 진입점
 *
 * version: 스키마 변경 시 반드시 올려야 함
 *          올리지 않으면 런타임에 IllegalStateException 발생
 *          버전을 올릴 때는 DatabaseModule에 Migration도 함께 작성
 */
@Database(
    entities = [
        LikedEtfEntity::class,
        // 테이블 추가 시 여기에 등록
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun likedEtfDao(): LikedEtfDao

    // DAO 추가 시 아래에 동일한 패턴으로 추가
    // abstract fun xxxDao(): XxxDao
}