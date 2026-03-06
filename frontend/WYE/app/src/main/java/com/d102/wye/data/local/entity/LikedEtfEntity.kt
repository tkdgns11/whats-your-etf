package com.d102.wye.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 관심 ETF 로컬 캐시 Entity
 * 서버 없이도 관심 목록을 즉시 보여주기 위해 Room에 저장
 */
@Entity(tableName = "liked_etf")
data class LikedEtfEntity(
    @PrimaryKey
    @ColumnInfo(name = "ticker")
    val ticker: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "current_price")
    val currentPrice: Long,

    @ColumnInfo(name = "change_rate")
    val changeRate: Double,

    @ColumnInfo(name = "volume")
    val volume: Long,

    @ColumnInfo(name = "risk_level")
    val riskLevel: Int,

    @ColumnInfo(name = "investment_strategy")
    val investmentStrategy: String,

    @ColumnInfo(name = "asset_class")
    val assetClass: String,

    @ColumnInfo(name = "dividend_yield")
    val dividendYield: Double
)