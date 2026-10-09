package com.shiv.executor.services.db
import androidx.room.*
@Dao interface PositionDao {
 @Query("SELECT * FROM positions") suspend fun getAll():List<PositionEntity>
 @Query("SELECT * FROM positions WHERE moduleId=:id LIMIT 1") suspend fun getById(id:String):PositionEntity?
 @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun insert(pos:PositionEntity)
 @Query("DELETE FROM positions WHERE moduleId=:id") suspend fun delete(id:String)
 @Query("SELECT * FROM trade_history ORDER BY timestamp DESC LIMIT 200") suspend fun getHistory():List<TradeHistoryEntity>
 @Query("SELECT * FROM trade_history ORDER BY timestamp DESC") suspend fun getAllHistory():List<TradeHistoryEntity>
 @Insert suspend fun insertHistory(h:TradeHistoryEntity)
}