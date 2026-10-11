package com.shiv.executor.services.db
import androidx.room.*
@Dao
interface PositionDao {
    @Query("SELECT * FROM positions") suspend fun getAll(): List<PositionEntity>
    @Query("SELECT * FROM positions") suspend fun getAllPositions(): List<PositionEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(position: PositionEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertPosition(position: PositionEntity)
    @Query("DELETE FROM positions WHERE moduleId = :moduleId") suspend fun delete(moduleId: String)
    @Query("DELETE FROM positions WHERE moduleId = :moduleId") suspend fun deleteById(moduleId: String)
    @Query("SELECT * FROM trade_history ORDER BY timestamp DESC") suspend fun getAllHistory(): List<TradeHistoryEntity>
    @Query("SELECT * FROM trade_history ORDER BY timestamp DESC") suspend fun getHistory(): List<TradeHistoryEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertHistory(history: TradeHistoryEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertTradeHistory(history: TradeHistoryEntity)
}
