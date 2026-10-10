package com.shiv.executor.services.db
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PositionEntity::class, TradeHistoryEntity::class], version = 3, exportSchema = false)
abstract class ShivDatabase : RoomDatabase() {
    abstract fun positionDao(): PositionDao
}
