package com.routina.app.domain.model

import java.time.LocalDate

data class RoutineCompletion(
    val routineId: String,
    val scheduledDate: LocalDate,
    val completedAtEpochMillis: Long,
    val awardedXp: Int,
    val awardedPoints: Int,
)
