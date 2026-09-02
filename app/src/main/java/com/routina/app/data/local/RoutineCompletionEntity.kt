package com.routina.app.data.local

import androidx.room.Entity

@Entity(
    tableName = "routine_completions",
    primaryKeys = ["routineId", "scheduledEpochDay"],
)
data class RoutineCompletionEntity(
    val routineId: String,
    val scheduledEpochDay: Long,
    val completedAtEpochMillis: Long,
    val awardedXp: Int,
    val awardedPoints: Int,
)
