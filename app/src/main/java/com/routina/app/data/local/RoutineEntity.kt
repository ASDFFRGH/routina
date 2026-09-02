package com.routina.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey val id: String,
    val name: String,
    val startEpochDay: Long,
    val intervalDays: Long,
    val rewardXp: Int,
    val rewardPoints: Int,
    val createdAtEpochMillis: Long,
    val archivedEpochDay: Long?,
)
