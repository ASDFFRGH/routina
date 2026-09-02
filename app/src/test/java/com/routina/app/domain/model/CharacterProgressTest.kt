package com.routina.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CharacterProgressTest {
    @Test
    fun derivesLevelAndProgressOnXpBoundaries() {
        val levelOne = CharacterProgress(0)
        val levelTwo = CharacterProgress(100)
        val nearLevelTwo = CharacterProgress(199)

        assertEquals(1, levelOne.level)
        assertEquals(0, levelOne.currentLevelXp)
        assertEquals(100, levelOne.nextLevelXp)
        assertEquals(0f, levelOne.progress, 0f)
        assertEquals(2, levelTwo.level)
        assertEquals(0, levelTwo.currentLevelXp)
        assertEquals(99, nearLevelTwo.currentLevelXp)
        assertTrue(nearLevelTwo.progress > 0.98f)
    }

    @Test
    fun derivesStagesAtLevelsOneFiveAndTen() {
        assertEquals(CharacterStage.NOVICE, CharacterProgress(0).stage)
        assertEquals(CharacterStage.ADVENTURER, CharacterProgress(400).stage)
        assertEquals(CharacterStage.MASTER, CharacterProgress(900).stage)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNegativeXp() {
        CharacterProgress(-1)
    }
}
