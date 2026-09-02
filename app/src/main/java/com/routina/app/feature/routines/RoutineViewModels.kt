package com.routina.app.feature.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.routina.app.domain.model.Routine
import com.routina.app.domain.repository.RoutineRepository
import java.time.LocalDate
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RoutineListViewModel(
    private val repository: RoutineRepository,
    private val todayProvider: () -> LocalDate = LocalDate::now,
) : ViewModel() {
    val routines: StateFlow<List<Routine>> = repository.observeRoutines()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun archive(routine: Routine) {
        viewModelScope.launch { archiveIfActive(routine) }
    }

    internal suspend fun archiveIfActive(routine: Routine) {
        if (!routine.isActive()) return
        repository.archiveRoutine(routine.id, effectiveArchiveDate())
    }

    /**
     * Recurrence excludes its archive date, so archive on the following day to retain
     * today's schedule and completion history in the calendar.
     */
    internal fun effectiveArchiveDate(): LocalDate = todayProvider().plusDays(1)
}

class RoutineFormViewModel(
    private val repository: RoutineRepository,
    private val now: () -> Long = { System.currentTimeMillis() },
) : ViewModel() {
    private val mutableState = kotlinx.coroutines.flow.MutableStateFlow(RoutineFormState())
    val state: StateFlow<RoutineFormState> = mutableState.asStateFlow()

    private val savedChannel = Channel<Unit>(Channel.CONFLATED)
    val saved = savedChannel.receiveAsFlow()

    fun updateName(value: String) = mutableState.update { it.copy(name = value) }

    fun updateStartDate(value: LocalDate) = mutableState.update { it.copy(startDate = value) }

    fun updatePreset(value: FrequencyPreset) = mutableState.update { it.copy(preset = value) }

    fun updateCustomInterval(value: String) = mutableState.update { it.copy(customInterval = value) }

    fun save() {
        val current = mutableState.value
        val frequency = frequencyFor(current.preset, current.customInterval) ?: return
        if (current.name.isBlank() || current.isSaving) return
        mutableState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                repository.createRoutine(
                    Routine(
                        name = current.name.trim(),
                        startDate = current.startDate,
                        frequency = frequency,
                        rewardXp = REWARD_XP,
                        rewardPoints = REWARD_POINTS,
                        createdAtEpochMillis = now(),
                    ),
                )
                savedChannel.trySend(Unit)
            } finally {
                mutableState.update { it.copy(isSaving = false) }
            }
        }
    }

    private companion object {
        const val REWARD_XP = 20
        const val REWARD_POINTS = 10
    }
}

class RoutineListViewModelFactory(
    private val repository: RoutineRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = RoutineListViewModel(repository) as T
}

class RoutineFormViewModelFactory(
    private val repository: RoutineRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = RoutineFormViewModel(repository) as T
}
