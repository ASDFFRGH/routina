package com.routina.app.feature.character

import com.routina.app.domain.model.CharacterStage
import com.routina.app.domain.model.Profile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class CharacterUiStateTest {
    @Test
    fun `maps profile rewards and novice progress`() {
        val state = Profile(totalXp = 42, totalPoints = 120).toCharacterUiState()

        assertEquals(1, state.level)
        assertEquals(120, state.totalPoints)
        assertEquals(42, state.totalXp)
        assertEquals(42, state.currentLevelXp)
        assertEquals(58, state.xpToNextLevel)
        assertEquals(.42f, state.progress)
        assertSame(CharacterStage.NOVICE, state.stage)
    }

    @Test
    fun `maps level boundary to a full next level requirement`() {
        val state = Profile(totalXp = 400, totalPoints = 99).toCharacterUiState()

        assertEquals(5, state.level)
        assertEquals(0, state.currentLevelXp)
        assertEquals(100, state.xpToNextLevel)
        assertEquals(0f, state.progress)
        assertSame(CharacterStage.ADVENTURER, state.stage)
    }

    @Test
    fun `maps master stage`() {
        val state = Profile(totalXp = 900).toCharacterUiState()

        assertEquals(10, state.level)
        assertSame(CharacterStage.MASTER, state.stage)
    }
}
