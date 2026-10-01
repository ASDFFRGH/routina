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
    fun `saved sort order takes precedence over creation time for pending and next routine`() {
        val older = routine(id = "older", createdAt = 1, sortOrder = 2)
        val newer = routine(id = "newer", createdAt = 2, sortOrder = 1)

        val state = todayUiState(date, listOf(older, newer), emptyList())

        assertEquals(listOf("newer", "older"), state.pending.map { it.routine.id })
        assertEquals("newer", state.nextRoutine?.routine?.id)
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

    private fun routine(
        id: String,
        createdAt: Long,
        frequency: Frequency = Frequency.EveryDays(1),
        start: LocalDate = date,
        sortOrder: Long = createdAt,
    ) = Routine(
        id = id,
        name = id,
        startDate = start,
        frequency = frequency,
        rewardXp = 5,
        rewardPoints = 1,
        createdAtEpochMillis = createdAt,
        sortOrder = sortOrder,
    )
}
