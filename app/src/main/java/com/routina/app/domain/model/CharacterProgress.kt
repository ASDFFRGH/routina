package com.routina.app.domain.model

/**
 * Character growth uses a fixed 100 XP per level curve. Level 1 starts at 0 XP;
 * 100 XP advances the character to level 2. Stages change at levels 5 and 10.
 */
data class CharacterProgress(val totalXp: Int) {
    init {
        require(totalXp >= 0) { "totalXp cannot be negative" }
    }

    val level: Int = totalXp / XP_PER_LEVEL + 1
    val currentLevelXp: Int = totalXp % XP_PER_LEVEL
    val nextLevelXp: Int = XP_PER_LEVEL
    val progress: Float = currentLevelXp.toFloat() / nextLevelXp
    val stage: CharacterStage = when {
        level >= 10 -> CharacterStage.MASTER
        level >= 5 -> CharacterStage.ADVENTURER
        else -> CharacterStage.NOVICE
    }

    private companion object {
        const val XP_PER_LEVEL = 100
    }
}

enum class CharacterStage {
    NOVICE,
    ADVENTURER,
    MASTER,
}
