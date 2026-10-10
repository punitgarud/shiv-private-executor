package com.shiv.executor.services.db
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "positions")
data class PositionEntity(
    @PrimaryKey val moduleId: String,
    val entryPrice: Double,
    val size: Double,
    val side: String,
    val timestamp: Long = System.currentTimeMillis()
)
