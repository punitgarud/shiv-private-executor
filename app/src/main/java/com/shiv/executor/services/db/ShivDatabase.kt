package com.shiv.executor.services.db
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PositionEntity::class, TradeHistoryEntity::class], version = 4, exportSchema = false)
abstract class ShivDatabase : RoomDatabase() {
    abstract fun positionDao(): PositionDao
    companion object {
        @Volatile private var INSTANCE: ShivDatabase? = null
        fun getDatabase(context: Context): ShivDatabase {
            return INSTANCE?: synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext, ShivDatabase::class.java, "shiv_database").fallbackToDestructiveMigration().build()
                INSTANCE = instance; instance
            }
        }
        fun get(context: Context) = getDatabase(context)
    }
}
