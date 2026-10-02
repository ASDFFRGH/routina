package com.routina.app.feature.character

import com.routina.app.domain.model.CharacterProgress
import com.routina.app.domain.model.CharacterStage
import com.routina.app.domain.model.Profile
import com.routina.app.domain.model.Routine
import com.routina.app.domain.model.RoutineCompletion
import com.routina.app.feature.routines.routineStreak
import java.time.LocalDate

/** Immutable display data for the character-growth screen. */
data class CharacterUiState(
    val level: Int,
    val totalPoints: Int,
    val totalXp: Int,
    val currentLevelXp: Int,
    val xpToNextLevel: Int,
    val progress: Float,
    val stage: CharacterStage,
    val streaks: List<CharacterRoutineStreak> = emptyList(),
) {
    companion object {
        val Initial = Profile().toCharacterUiState()
    }
}

/** Current streak per active routine. A day means one scheduled occurrence. */
data class CharacterRoutineStreak(
    val routineId: String,
    val name: String,
    val days: Int,
)

internal fun characterRoutineStreaks(
    routines: List<Routine>,
    completions: Collection<RoutineCompletion>,
    today: LocalDate,
): List<CharacterRoutineStreak> = routines.asSequence()
    .filter { it.archivedEpochDay == null }
    .sortedWith(compareBy { it.sortOrder })
    .map { routine -> CharacterRoutineStreak(routine.id, routine.name, routineStreak(routine, completions, today)) }
    .toList()

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
