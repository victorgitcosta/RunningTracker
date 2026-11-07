package com.exemplo.runningtracker

import android.app.Application
import androidx.room.Room
import com.exemplo.runningtracker.data.local.RunDatabase
import com.exemplo.runningtracker.data.repository.RoomRunRepository

/** Injeção de dependências simples, sem framework de DI. */
class RunningTrackerApplication : Application() {
    private val database: RunDatabase by lazy {
        Room.databaseBuilder(this, RunDatabase::class.java, "running_tracker.db").build()
    }
    val repository: RoomRunRepository by lazy { RoomRunRepository(database.runDao()) }
}
