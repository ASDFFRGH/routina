package com.routina.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class FrequencyTest {
    @Test
    fun predefinedFrequenciesExposeExpectedIntervals() {
        assertEquals(1, Frequency.Daily.intervalDays)
        assertEquals(3, Frequency.EveryThreeDays.intervalDays)
        assertEquals(7, Frequency.Weekly.intervalDays)
    }

    @Test(expected = IllegalArgumentException::class)
    fun customFrequencyRejectsZeroDayInterval() {
        Frequency.EveryDays(0)
    }
}
