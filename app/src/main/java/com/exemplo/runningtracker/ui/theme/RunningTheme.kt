package com.exemplo.runningtracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Lime = Color(0xFFB8F000)
val LimeSoft = Color(0xFFF1F9D8)
val Ink = Color(0xFF17191A)
val Muted = Color(0xFF919593)
val ScreenBackground = Color(0xFFFBFBF8)

private val Palette = lightColorScheme(
    primary = Lime,
    onPrimary = Ink,
    background = ScreenBackground,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    secondary = Ink
)

@Composable
fun RunningTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Palette, content = content)
}
