package com.routina.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profile WHERE id = ${ProfileEntity.PROFILE_ID}")
    fun observe(): Flow<ProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(profile: ProfileEntity): Long

    @Query("UPDATE profile SET totalXp = totalXp + :xp, totalPoints = totalPoints + :points WHERE id = ${ProfileEntity.PROFILE_ID}")
    suspend fun addRewards(xp: Int, points: Int)

    @Query("UPDATE profile SET totalXp = MAX(0, totalXp - :xp), totalPoints = MAX(0, totalPoints - :points) WHERE id = ${ProfileEntity.PROFILE_ID}")
    suspend fun removeRewards(xp: Int, points: Int)
}
