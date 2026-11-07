package com.exemplo.runningtracker.domain.util

import kotlin.math.roundToInt

object MetricsCalculator {
    // Valor didático. A estimativa simplificada usa 70 kg * km percorridos.
    private const val ASSUMED_WEIGHT_KG = 70f
    const val DEMO_BPM = 132

    fun distanceKm(distanceMeters: Float): Float = distanceMeters / 1_000f

    fun calories(distanceMeters: Float): Int =
        (distanceKm(distanceMeters) * ASSUMED_WEIGHT_KG).roundToInt().coerceAtLeast(0)

    fun paceMinutesPerKm(durationMillis: Long, distanceMeters: Float): Float =
        if (distanceMeters < 1f) 0f
        else (durationMillis / 60_000f) / distanceKm(distanceMeters)
}
