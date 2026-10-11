package com.shiv.executor.services.db
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "trade_history")
data class TradeHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val moduleId: String,
    val moduleName: String = "",
    val type: String = "BUY",
    val action: String = "BUY",
    val amountSOL: Double = 0.0,
    val scoreAtTrade: Double = 0.0,
    val pnlPct: Double = 0.0,
    val pnl: Double = 0.0,
    val price: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val txSig: String = ""
)
