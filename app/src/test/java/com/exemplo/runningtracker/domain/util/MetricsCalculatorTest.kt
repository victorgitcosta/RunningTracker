package com.exemplo.runningtracker.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

class MetricsCalculatorTest {
    @Test fun `1 km em 5 minutos equivale a pace 5`() {
        assertEquals(5f, MetricsCalculator.paceMinutesPerKm(300_000L, 1_000f), 0.001f)
    }

    @Test fun `sem deslocamento pace e zero`() {
        assertEquals(0f, MetricsCalculator.paceMinutesPerKm(300_000L, 0f), 0.001f)
    }

    @Test fun `estimativa calorica de 3 km`() {
        assertEquals(210, MetricsCalculator.calories(3_000f))
    }
}
