package com.routina.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val id: Int = PROFILE_ID,
    val totalXp: Int = 0,
    val totalPoints: Int = 0,
) {
    companion object {
        const val PROFILE_ID = 1
    }
}
