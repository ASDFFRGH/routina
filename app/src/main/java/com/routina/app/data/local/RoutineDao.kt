package com.routina.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines ORDER BY sortOrder ASC, createdAtEpochMillis ASC, id ASC")
    fun observeAll(): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM routines WHERE id = :id")
    suspend fun findById(id: String): RoutineEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(routine: RoutineEntity)

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM routines")
    suspend fun maxSortOrder(): Long

    @Query("SELECT id FROM routines WHERE archivedEpochDay IS NULL ORDER BY sortOrder ASC, createdAtEpochMillis ASC, id ASC")
    suspend fun activeIds(): List<String>

    @Query("UPDATE routines SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: String, sortOrder: Long): Int

    @Query("UPDATE routines SET archivedEpochDay = :archivedEpochDay WHERE id = :id")
    suspend fun archive(id: String, archivedEpochDay: Long): Int
}
