package com.routina.app.feature.today

import com.routina.app.domain.model.Frequency
import com.routina.app.domain.model.Profile
import com.routina.app.domain.model.Routine
import com.routina.app.domain.model.RoutineCompletion
import com.routina.app.domain.repository.CompletionResult
import com.routina.app.domain.repository.RoutineRepository
import java.time.LocalDate
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModel
import org.junit.Test

class TodayViewModelTest {
    private val today = LocalDate.of(2026, 9, 6)

    @Test
    fun stale_day_tap_refreshes_without_writing() = runBlocking<Unit> {
        val repository = TodayFakeRepository()
        var currentDate = today
        val store = ViewModelStore()
        val viewModel = ViewModelProvider(store, factory(repository) { currentDate })[TodayViewModel::class.java]
        val collector = launch { viewModel.uiState.collect() }
        try {
            val item = TodayRoutine(routine(), today, false)
            currentDate = today.plusDays(1)

            viewModel.complete(item)

            withTimeout(2_000) { viewModel.uiState.first { it.date == currentDate } }
            assertEquals(0, repository.completeCalls)
        } finally {
            collector.cancelAndJoin()
            store.clear()
        }
    }

    @Test
    fun ignores_second_tap_while_first_write_is_running() = runBlocking<Unit> {
        val repository = TodayFakeRepository().apply { completeGate = CompletableDeferred() }
        val store = ViewModelStore()
        val viewModel = ViewModelProvider(store, factory(repository) { today })[TodayViewModel::class.java]
        val collector = launch { viewModel.uiState.collect() }
        try {
            val item = TodayRoutine(routine(), today, false)

            viewModel.complete(item)
            withTimeout(2_000) { repository.completeStarted.await() }
            viewModel.complete(item)
            assertEquals(1, repository.completeCalls)
            repository.completeGate?.complete(Unit)
            withTimeout(2_000) { viewModel.uiState.first { !it.isProcessing && repository.completeCalls == 1 } }
        } finally {
            collector.cancelAndJoin()
            store.clear()
        }
    }

    @Test
    fun observe_failure_can_retry_and_write_failure_reenables_actions() = runBlocking<Unit> {
        val repository = TodayFakeRepository().apply { failObserve = true }
        val store = ViewModelStore()
        val viewModel = ViewModelProvider(store, factory(repository) { today })[TodayViewModel::class.java]
        val collector = launch { viewModel.uiState.collect() }
        try {
            withTimeout(2_000) { viewModel.uiState.first { it.errorMessage != null } }
            repository.failObserve = false
            viewModel.retry()
            withTimeout(2_000) { viewModel.uiState.first { !it.isLoading && it.errorMessage == null } }

            repository.completeFailure = IllegalStateException("disk")
            viewModel.complete(TodayRoutine(routine(), today, false))
            withTimeout(2_000) { viewModel.uiState.first { it.errorMessage != null && !it.isProcessing } }
            repository.completeFailure = null
            viewModel.complete(TodayRoutine(routine(), today, false))
            withTimeout(2_000) { viewModel.uiState.first { repository.completeCalls == 2 && !it.isProcessing && it.errorMessage == null } }
            assertEquals(2, repository.completeCalls)
        } finally {
            collector.cancelAndJoin()
            store.clear()
        }
    }

    @Test
    fun cancelling_an_already_cancelled_routine_is_a_successful_no_op() = runBlocking<Unit> {
        val repository = TodayFakeRepository().apply { cancelResult = false }
        val store = ViewModelStore()
        val viewModel = ViewModelProvider(store, factory(repository) { today })[TodayViewModel::class.java]
        val collector = launch { viewModel.uiState.collect() }
        try {
            viewModel.cancel(TodayRoutine(routine(), today, true))

            val state = withTimeout(2_000) {
                viewModel.uiState.first { repository.cancelCalls == 1 && !it.isProcessing }
            }
            assertEquals(null, state.errorMessage)
        } finally {
            collector.cancelAndJoin()
            store.clear()
        }
    }

    @Test
    fun refresh_to_a_new_day_clears_an_old_action_error() = runBlocking<Unit> {
        val repository = TodayFakeRepository().apply { completeFailure = IllegalStateException("disk") }
        var currentDate = today
        val store = ViewModelStore()
        val viewModel = ViewModelProvider(store, factory(repository) { currentDate })[TodayViewModel::class.java]
        val collector = launch { viewModel.uiState.collect() }
        try {
            viewModel.complete(TodayRoutine(routine(), today, false))
            withTimeout(2_000) { viewModel.uiState.first { it.errorMessage != null && !it.isProcessing } }

            currentDate = today.plusDays(1)
            viewModel.refreshToday()
            val refreshed = withTimeout(2_000) {
                viewModel.uiState.first { it.date == currentDate && it.errorMessage == null }
            }
            assertEquals(currentDate, refreshed.date)
        } finally {
            collector.cancelAndJoin()
            store.clear()
        }
    }

    private fun routine() = Routine(
        id = "routine",
        name = "読書",
        startDate = today,
        frequency = Frequency.EveryDays(1),
        rewardXp = 1,
        rewardPoints = 1,
        createdAtEpochMillis = 1,
    )

    private fun factory(
        repository: RoutineRepository,
        todayProvider: () -> LocalDate,
    ) = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TodayViewModel(repository, todayProvider = todayProvider) as T
    }
}

private class TodayFakeRepository : RoutineRepository {
    var failObserve = false
    var completeFailure: Throwable? = null
    var completeCalls = 0
    var cancelCalls = 0
    var cancelResult = true
    val completeStarted = CompletableDeferred<Unit>()
    var completeGate: CompletableDeferred<Unit>? = null
    private val routines = MutableStateFlow(emptyList<Routine>())
    private val completions = MutableStateFlow(emptyList<RoutineCompletion>())

    override fun observeRoutines(): Flow<List<Routine>> =
        if (failObserve) flow { throw IllegalStateException("read") } else routines

    override fun observeCompletions(from: LocalDate, to: LocalDate): Flow<List<RoutineCompletion>> = completions
    override fun observeProfile(): Flow<Profile> = flowOf(Profile())
    override suspend fun createRoutine(routine: Routine) = Unit
    override suspend fun archiveRoutine(routineId: String, archivedOn: LocalDate) = Unit
    override suspend fun completeRoutine(routineId: String, scheduledDate: LocalDate, completedAtEpochMillis: Long): CompletionResult {
        completeCalls++
        completeStarted.complete(Unit)
        completeGate?.await()
        completeFailure?.let { throw it }
        return CompletionResult.COMPLETED
    }
    override suspend fun cancelCompletion(routineId: String, scheduledDate: LocalDate): Boolean {
        cancelCalls++
        return cancelResult
    }
}
