package com.shiv.executor.services.db
import androidx.room.*

@Dao
interface PositionDao {
    @Query("SELECT * FROM positions")
    suspend fun getAll(): List<PositionEntity>

    @Query("SELECT * FROM trade_history ORDER BY timestamp DESC")
    suspend fun getHistory(): List<TradeHistoryEntity>

    @Query("SELECT * FROM trade_history ORDER BY timestamp DESC")
    suspend fun getAllHistory(): List<TradeHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosition(pos: PositionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pos: PositionEntity)

    @Insert
    suspend fun insertHistory(trade: TradeHistoryEntity)

    @Query("DELETE FROM positions WHERE moduleId = :id")
    suspend fun deletePosition(id: String)

    @Query("DELETE FROM positions WHERE moduleId = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM positions WHERE moduleId = :id LIMIT 1")
    suspend fun getById(id: String): PositionEntity?
}