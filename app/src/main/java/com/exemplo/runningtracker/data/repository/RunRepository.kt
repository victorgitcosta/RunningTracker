package com.exemplo.runningtracker.data.repository

import com.exemplo.runningtracker.data.local.RunDao
import com.exemplo.runningtracker.data.local.RunEntity
import kotlinx.coroutines.flow.Flow

interface RunRepository {
    suspend fun insertRun(run: RunEntity)
    fun runsDescending(): Flow<List<RunEntity>>
    fun runsAscending(): Flow<List<RunEntity>>
    fun runsOnDate(startInclusive: Long, endExclusive: Long): Flow<List<RunEntity>>
}

class RoomRunRepository(private val dao: RunDao) : RunRepository {
    override suspend fun insertRun(run: RunEntity) = dao.insertRun(run)
    override fun runsDescending() = dao.getAllRunsSortedByDateDesc()
    override fun runsAscending() = dao.getAllRunsSortedByDateAsc()
    override fun runsOnDate(startInclusive: Long, endExclusive: Long) =
        dao.getRunsBySpecificDate(startInclusive, endExclusive)
}
