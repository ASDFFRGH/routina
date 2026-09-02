package com.routina.app.domain.schedule

import com.routina.app.domain.model.Frequency
import com.routina.app.domain.model.Routine
import java.time.LocalDate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecurrenceCalculatorTest {
    @Test
    fun schedulesEveryThreeDaysAcrossMonthAndYearBoundaries() {
        val routine = routine(startDate = LocalDate.of(2025, 12, 30), frequency = Frequency.EveryThreeDays)

        assertTrue(RecurrenceCalculator.isScheduledOn(routine, LocalDate.of(2025, 12, 30)))
        assertFalse(RecurrenceCalculator.isScheduledOn(routine, LocalDate.of(2025, 12, 31)))
        assertTrue(RecurrenceCalculator.isScheduledOn(routine, LocalDate.of(2026, 1, 2)))
        assertTrue(RecurrenceCalculator.isScheduledOn(routine, LocalDate.of(2026, 1, 5)))
    }

    @Test
    fun handlesLeapDayUsingCalendarDayDifference() {
        val routine = routine(startDate = LocalDate.of(2024, 2, 28), frequency = Frequency.EveryDays(2))

        assertTrue(RecurrenceCalculator.isScheduledOn(routine, LocalDate.of(2024, 2, 28)))
        assertFalse(RecurrenceCalculator.isScheduledOn(routine, LocalDate.of(2024, 2, 29)))
        assertTrue(RecurrenceCalculator.isScheduledOn(routine, LocalDate.of(2024, 3, 1)))
    }

    @Test
    fun doesNotScheduleBeforeStartDate() {
        val routine = routine(startDate = LocalDate.of(2026, 1, 10))

        assertFalse(RecurrenceCalculator.isScheduledOn(routine, LocalDate.of(2026, 1, 9)))
    }

    @Test
    fun archiveDateAndLaterDatesAreNotScheduledButPastDatesRemain() {
        val routine = routine(
            startDate = LocalDate.of(2026, 1, 1),
            archivedEpochDay = LocalDate.of(2026, 1, 7).toEpochDay(),
        )

        assertTrue(RecurrenceCalculator.isScheduledOn(routine, LocalDate.of(2026, 1, 6)))
        assertFalse(RecurrenceCalculator.isScheduledOn(routine, LocalDate.of(2026, 1, 7)))
        assertFalse(RecurrenceCalculator.isScheduledOn(routine, LocalDate.of(2026, 1, 8)))
    }

    private fun routine(
        startDate: LocalDate,
        frequency: Frequency = Frequency.Daily,
        archivedEpochDay: Long? = null,
    ) = Routine(
        name = "Read",
        startDate = startDate,
        frequency = frequency,
        rewardXp = 10,
        rewardPoints = 5,
        createdAtEpochMillis = 0,
        archivedEpochDay = archivedEpochDay,
    )
}
