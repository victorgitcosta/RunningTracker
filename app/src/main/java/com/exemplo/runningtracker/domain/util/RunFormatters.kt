package com.exemplo.runningtracker.domain.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

object RunFormatters {
    fun duration(timeMillis: Long): String {
        val seconds = timeMillis / 1_000
        val hours = seconds / 3_600
        val minutes = (seconds % 3_600) / 60
        val remaining = seconds % 60
        return if (hours > 0) "%02d:%02d:%02d".format(Locale.US, hours, minutes, remaining)
        else "%02d:%02d".format(Locale.US, minutes, remaining)
    }

    fun pace(minutesPerKm: Float): String {
        if (!minutesPerKm.isFinite() || minutesPerKm <= 0f) return "--:--"
        val totalSeconds = (minutesPerKm * 60).roundToInt()
        return "%d'%02d\"".format(Locale.US, totalSeconds / 60, totalSeconds % 60)
    }

    fun distance(meters: Float): String =
        String.format(Locale("pt", "BR"), "%.2f", meters / 1_000f)

    fun date(timestamp: Long): String =
        SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale("pt", "BR")).format(Date(timestamp))
}
