package com.shiv.executor.services.db
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "positions")
data class PositionEntity(
    @PrimaryKey val moduleId: String,
    val moduleName: String = "",
    val entryScore: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val amountSOL: Double = 0.0,
    val entryPrice: Double = 0.0,
    val mint: String = "",
    val size: Double = 0.0,
    val side: String = "BUY"
)
