package com.shiv.executor.services.db
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trade_history")
data class TradeHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val moduleId: String,
    val action: String,
    val price: Double,
    val pnl: Double,
    val timestamp: Long = System.currentTimeMillis()
)
