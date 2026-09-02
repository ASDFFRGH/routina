package com.routina.app.feature.character

import com.routina.app.domain.model.CharacterProgress
import com.routina.app.domain.model.CharacterStage
import com.routina.app.domain.model.Profile

/** Immutable display data for the character-growth screen. */
data class CharacterUiState(
    val level: Int,
    val totalPoints: Int,
    val totalXp: Int,
    val currentLevelXp: Int,
    val xpToNextLevel: Int,
    val progress: Float,
    val stage: CharacterStage,
) {
    companion object {
        val Initial = Profile().toCharacterUiState()
    }
}

/** Converts persisted rewards into presentation values without embedding UI concerns in the domain. */
fun Profile.toCharacterUiState(): CharacterUiState {
    val characterProgress = CharacterProgress(totalXp)
    return CharacterUiState(
        level = characterProgress.level,
        totalPoints = totalPoints,
        totalXp = totalXp,
        currentLevelXp = characterProgress.currentLevelXp,
        xpToNextLevel = characterProgress.nextLevelXp - characterProgress.currentLevelXp,
        progress = characterProgress.progress,
        stage = characterProgress.stage,
    )
}
