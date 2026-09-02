package com.routina.app.domain.model

import java.time.LocalDate
import java.util.UUID

data class Routine(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val startDate: LocalDate,
    val frequency: Frequency,
    val rewardXp: Int,
    val rewardPoints: Int,
    val createdAtEpochMillis: Long,
    val archivedEpochDay: Long? = null,
)
