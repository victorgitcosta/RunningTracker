package com.exemplo.runningtracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Uma linha por corrida concluída. routeCoordinates é uma lista em formato JSON. */
@Entity(tableName = "runs")
data class RunEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val durationInMillis: Long,
    val distanceInMeters: Float,
    val caloriesBurned: Int,
    val avgPace: Float,
    val avgBpm: Int,
    val routeCoordinates: String
)
