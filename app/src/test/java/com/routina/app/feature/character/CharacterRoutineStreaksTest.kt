package com.routina.app.feature.character

import com.routina.app.domain.model.Frequency
import com.routina.app.domain.model.Routine
import com.routina.app.domain.model.RoutineCompletion
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class CharacterRoutineStreaksTest {
    private val today = LocalDate.of(2026, 9, 3)

    @Test
    fun `projects active routines in order including zero streak and excludes archived`() {
        val active = routine("active", "読書", 0)
        val zero = routine("zero", "運動", 1)
        val archived = routine("archived", "過去", 2).copy(archivedEpochDay = today.toEpochDay())

        assertEquals(
            listOf(CharacterRoutineStreak("active", "読書", 2), CharacterRoutineStreak("zero", "運動", 0)),
            characterRoutineStreaks(
                listOf(zero, archived, active),
                listOf(RoutineCompletion("active", today.minusDays(2), 0, 1, 1), RoutineCompletion("active", today.minusDays(1), 0, 1, 1)),
                today,
            ),
        )
    }

    private fun routine(id: String, name: String, order: Long) = Routine(
        id = id, name = name, startDate = today.minusDays(2), frequency = Frequency.Daily,
        rewardXp = 1, rewardPoints = 1, createdAtEpochMillis = order, sortOrder = order,
    )
}
