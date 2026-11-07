package com.exemplo.runningtracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RunDao {
    @Insert
    suspend fun insertRun(run: RunEntity)

    @Query("SELECT * FROM runs ORDER BY timestamp DESC")
    fun getAllRunsSortedByDateDesc(): Flow<List<RunEntity>>

    @Query("SELECT * FROM runs ORDER BY timestamp ASC")
    fun getAllRunsSortedByDateAsc(): Flow<List<RunEntity>>

    @Query("SELECT * FROM runs WHERE timestamp >= :startOfDay AND timestamp < :endOfDay ORDER BY timestamp DESC")
    fun getRunsBySpecificDate(startOfDay: Long, endOfDay: Long): Flow<List<RunEntity>>
}
