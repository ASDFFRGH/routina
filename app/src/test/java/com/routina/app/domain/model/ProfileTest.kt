package com.routina.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileTest {
    @Test
    fun defaultsToNoProgress() {
        assertEquals(Profile(totalXp = 0, totalPoints = 0), Profile())
    }
}
