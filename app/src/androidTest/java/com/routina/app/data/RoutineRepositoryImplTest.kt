package com.routina.app.data

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.routina.app.data.local.RoutinaDatabase
import com.routina.app.domain.model.Frequency
import com.routina.app.domain.model.Routine
import com.routina.app.domain.repository.CompletionResult
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RoutineRepositoryImplTest {
    private lateinit var database: RoutinaDatabase
    private lateinit var repository: RoutineRepositoryImpl

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, RoutinaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = RoutineRepositoryImpl(database) { LocalDate.of(2026, 9, 3) }
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun completionIsRecordedOnceAndItsRewardIsAwardedOnce() = runBlocking {
        val routine = routine(startDate = LocalDate.of(2026, 9, 1))
        repository.createRoutine(routine)

        assertEquals(
            CompletionResult.COMPLETED,
            repository.completeRoutine(routine.id, routine.startDate, completedAtEpochMillis = 100L),
        )
        assertEquals(
            CompletionResult.ALREADY_COMPLETED,
            repository.completeRoutine(routine.id, routine.startDate, completedAtEpochMillis = 200L),
        )

        assertEquals(30, repository.observeProfile().first().totalXp)
        assertEquals(7, repository.observeProfile().first().totalPoints)
        assertEquals(1, repository.observeCompletions(routine.startDate, routine.startDate).first().size)
    }

    @Test
    fun cancellingCompletionReversesStoredRewardAndPreservesHistoryRules() = runBlocking {
        val startDate = LocalDate.of(2026, 9, 1)
        val routine = routine(startDate)
        repository.createRoutine(routine)
        repository.completeRoutine(routine.id, startDate, completedAtEpochMillis = 100L)

        assertTrue(repository.cancelCompletion(routine.id, startDate))
        assertFalse(repository.cancelCompletion(routine.id, startDate))

        assertEquals(0, repository.observeProfile().first().totalXp)
        assertEquals(0, repository.observeProfile().first().totalPoints)
        assertTrue(repository.observeCompletions(startDate, startDate).first().isEmpty())
        assertEquals(1, repository.observeRoutines().first().size)
    }

    @Test
    fun invalidScheduledDateDoesNotCreateCompletionOrReward() = runBlocking {
        val routine = routine(LocalDate.of(2026, 9, 1), Frequency.EveryThreeDays)
        repository.createRoutine(routine)

        assertEquals(
            CompletionResult.NOT_SCHEDULED,
            repository.completeRoutine(routine.id, LocalDate.of(2026, 9, 2), completedAtEpochMillis = 100L),
        )

        assertEquals(0, repository.observeProfile().first().totalXp)
        assertTrue(
            repository.observeCompletions(routine.startDate, routine.startDate.plusDays(2)).first().isEmpty(),
        )
    }

    @Test
    fun futureCompletionDoesNotCreateCompletionOrChangeRewards() = runBlocking {
        val today = LocalDate.of(2026, 9, 3)
        val routine = routine(today)
        repository.createRoutine(routine)
        repository.completeRoutine(routine.id, today, completedAtEpochMillis = 100L)

        assertEquals(
            CompletionResult.FUTURE_DATE,
            repository.completeRoutine(routine.id, today.plusDays(1), completedAtEpochMillis = 200L),
        )

        assertEquals(30, repository.observeProfile().first().totalXp)
        assertEquals(7, repository.observeProfile().first().totalPoints)
        assertEquals(1, repository.observeCompletions(today, today.plusDays(1)).first().size)
    }

    private fun routine(startDate: LocalDate, frequency: Frequency = Frequency.Daily) = Routine(
        id = "routine-${startDate.toEpochDay()}-${frequency.intervalDays}",
        name = "Read",
        startDate = startDate,
        frequency = frequency,
        rewardXp = 30,
        rewardPoints = 7,
        createdAtEpochMillis = 1L,
    )
}
