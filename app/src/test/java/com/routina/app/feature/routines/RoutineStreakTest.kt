package com.routina.app.feature.routines

import com.routina.app.domain.model.Frequency
import com.routina.app.domain.model.Routine
import com.routina.app.domain.model.RoutineCompletion
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class RoutineStreakTest {
    private val start = LocalDate.of(2026, 9, 1)

    @Test fun `pending completion today retains preceding streak`() {
        val routine = routine(Frequency.Daily)
        assertEquals(2, routineStreak(routine, listOf(completion(0), completion(1)), start.plusDays(2)))
    }

    @Test fun `missed past scheduled occurrence resets streak`() {
        val routine = routine(Frequency.EveryThreeDays)
        assertEquals(0, routineStreak(routine, listOf(completion(0)), start.plusDays(7)))
    }

    @Test fun `interval based occurrences count as one each`() {
        val routine = routine(Frequency.EveryThreeDays)
        assertEquals(3, routineStreak(routine, listOf(completion(0), completion(3), completion(6)), start.plusDays(6)))
    }

    @Test fun `only completed occurrences after the latest miss form the streak`() {
        val routine = routine(Frequency.Daily)
        assertEquals(2, routineStreak(routine, listOf(completion(0), completion(2), completion(3)), start.plusDays(3)))
    }

    @Test fun `archived routine keeps final streak`() {
        val routine = routine(Frequency.Daily, archivedEpochDay = start.plusDays(3).toEpochDay())
        assertEquals(3, routineStreak(routine, listOf(completion(0), completion(1), completion(2)), start.plusDays(10)))
    }

    private fun routine(frequency: Frequency, archivedEpochDay: Long? = null) = Routine(
        id = "routine",
        name = "読書",
        startDate = start,
        frequency = frequency,
        rewardXp = 20,
        rewardPoints = 10,
        createdAtEpochMillis = 1,
        archivedEpochDay = archivedEpochDay,
    )

    private fun completion(offset: Long) = RoutineCompletion("routine", start.plusDays(offset), 0, 20, 10)
}
