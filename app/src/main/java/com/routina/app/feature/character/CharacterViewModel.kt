package com.routina.app.feature.character

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.routina.app.domain.repository.RoutineRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

class CharacterViewModel(
    private val repository: RoutineRepository,
    private val todayProvider: () -> LocalDate = LocalDate::now,
) : ViewModel() {
    private val currentToday = MutableStateFlow(todayProvider())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<CharacterUiState> = currentToday.flatMapLatest { today ->
        combine(
            repository.observeProfile(),
            repository.observeRoutines(),
            repository.observeCompletions(LocalDate.MIN, today),
        ) { profile, routines, completions ->
            profile.toCharacterUiState().copy(
                streaks = characterRoutineStreaks(routines, completions, today),
            )
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CharacterUiState.Initial,
        )

    fun refreshToday() {
        currentToday.value = todayProvider()
    }
}

class CharacterViewModelFactory(
    private val repository: RoutineRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(CharacterViewModel::class.java)) {
            "Unsupported ViewModel class: ${modelClass.name}"
        }
        return CharacterViewModel(repository) as T
    }
}
