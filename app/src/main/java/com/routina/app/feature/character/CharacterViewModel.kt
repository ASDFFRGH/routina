package com.routina.app.feature.character

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.routina.app.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class CharacterViewModel(repository: RoutineRepository) : ViewModel() {
    val uiState: StateFlow<CharacterUiState> = repository.observeProfile()
        .map { profile -> profile.toCharacterUiState() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CharacterUiState.Initial,
        )
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
