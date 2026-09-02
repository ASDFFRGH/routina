package com.routina.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines ORDER BY createdAtEpochMillis ASC")
    fun observeAll(): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM routines WHERE id = :id")
    suspend fun findById(id: String): RoutineEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(routine: RoutineEntity)

    @Query("UPDATE routines SET archivedEpochDay = :archivedEpochDay WHERE id = :id")
    suspend fun archive(id: String, archivedEpochDay: Long): Int
}
