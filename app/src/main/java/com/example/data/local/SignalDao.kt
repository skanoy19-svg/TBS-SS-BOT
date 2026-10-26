package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SignalDao {
    @Query("SELECT * FROM signals ORDER BY timestamp DESC")
    fun getAllSignals(): Flow<List<SignalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSignal(signal: SignalEntity): Long

    @Query("DELETE FROM signals WHERE id = :id")
    suspend fun deleteSignal(id: Long)

    @Query("DELETE FROM signals")
    suspend fun clearAllSignals()

    @Update
    suspend fun updateSignal(signal: SignalEntity)

    @Query("SELECT COUNT(*) FROM signals")
    fun getSignalCount(): Flow<Int>
}
