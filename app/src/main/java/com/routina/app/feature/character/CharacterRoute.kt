package com.routina.app.feature.character

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.routina.app.domain.repository.RoutineRepository

/** Navigation entry point for the character and rewards destination. */
@Composable
fun CharacterRoute(
    repository: RoutineRepository,
    modifier: Modifier = Modifier,
) {
    val viewModel: CharacterViewModel = viewModel(
        factory = CharacterViewModelFactory(repository),
    )
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    CharacterScreen(
        uiState = uiState.value,
        modifier = modifier,
    )
}
