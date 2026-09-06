package com.routina.app.feature.today

import com.routina.app.domain.model.Frequency
import com.routina.app.domain.model.Routine
import com.routina.app.domain.model.RoutineCompletion
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TodayPresentationTest {
    private val date = LocalDate.of(2026, 9, 6)

    @Test
    fun `maps only scheduled routines and orders by createdAt then id`() {
        val later = routine(id = "z", createdAt = 20)
        val tie = routine(id = "a", createdAt = 20)
        val first = routine(id = "b", createdAt = 10)
        val alternateDay = routine(id = "off", createdAt = 1, frequency = Frequency.EveryDays(2), start = date.minusDays(1))

        val state = todayUiState(date, listOf(later, alternateDay, tie, first), emptyList())

        assertEquals(listOf("b", "a", "z"), state.pending.map { it.routine.id })
        assertEquals(date, state.nextRoutine?.scheduledDate)
    }

    @Test
    fun `separates completed routines and recognizes all done`() {
        val first = routine(id = "first", createdAt = 1)
        val second = routine(id = "second", createdAt = 2)
        val completion = RoutineCompletion("first", date, 1L, 5, 1)

        val partial = todayUiState(date, listOf(first, second), listOf(completion))
        val complete = todayUiState(date, listOf(first, second), listOf(completion, completion.copy(routineId = "second")))

        assertEquals(listOf("second"), partial.pending.map { it.routine.id })
        assertEquals(listOf("first"), partial.completed.map { it.routine.id })
        assertFalse(partial.isAllDone)
        assertTrue(complete.isAllDone)
    }

    private fun routine(id: String, createdAt: Long, frequency: Frequency = Frequency.EveryDays(1), start: LocalDate = date) = Routine(
        id = id,
        name = id,
        startDate = start,
        frequency = frequency,
        rewardXp = 5,
        rewardPoints = 1,
        createdAtEpochMillis = createdAt,
    )
}
