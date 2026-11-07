package com.exemplo.runningtracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [RunEntity::class], version = 1, exportSchema = false)
@TypeConverters(RouteJsonConverter::class)
abstract class RunDatabase : RoomDatabase() {
    abstract fun runDao(): RunDao
}
