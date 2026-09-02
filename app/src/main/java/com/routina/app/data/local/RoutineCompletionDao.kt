package com.routina.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineCompletionDao {
    @Query("SELECT * FROM routine_completions WHERE scheduledEpochDay BETWEEN :fromEpochDay AND :toEpochDay ORDER BY scheduledEpochDay ASC")
    fun observeBetween(fromEpochDay: Long, toEpochDay: Long): Flow<List<RoutineCompletionEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(completion: RoutineCompletionEntity): Long

    @Query("SELECT * FROM routine_completions WHERE routineId = :routineId AND scheduledEpochDay = :scheduledEpochDay")
    suspend fun find(routineId: String, scheduledEpochDay: Long): RoutineCompletionEntity?

    @Query("DELETE FROM routine_completions WHERE routineId = :routineId AND scheduledEpochDay = :scheduledEpochDay")
    suspend fun delete(routineId: String, scheduledEpochDay: Long): Int
}
