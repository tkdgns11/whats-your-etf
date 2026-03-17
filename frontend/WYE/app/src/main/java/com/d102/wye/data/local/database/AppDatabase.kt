package com.d102.wye.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.d102.wye.data.local.dao.EtfPriceHistoryDao
import com.d102.wye.data.local.dao.LikedEtfDao
import com.d102.wye.data.local.entity.EtfPriceHistoryEntity
import com.d102.wye.data.local.entity.LikedEtfEntity

@Database(
    entities = [
        LikedEtfEntity::class,
        EtfPriceHistoryEntity::class,
//        EtfFundamentalsEntity::class,
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun likedEtfDao(): LikedEtfDao
    abstract fun etfPriceHistoryDao(): EtfPriceHistoryDao
//    abstract fun etfFundamentalsDao(): EtfFundamentalsDao
}