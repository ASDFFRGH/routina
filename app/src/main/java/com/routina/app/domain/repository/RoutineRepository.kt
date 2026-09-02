package com.routina.app.domain.repository

import com.routina.app.domain.model.Profile
import com.routina.app.domain.model.Routine
import com.routina.app.domain.model.RoutineCompletion
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface RoutineRepository {
    fun observeRoutines(): Flow<List<Routine>>

    fun observeCompletions(from: LocalDate, to: LocalDate): Flow<List<RoutineCompletion>>

    fun observeProfile(): Flow<Profile>

    suspend fun createRoutine(routine: Routine)

    suspend fun archiveRoutine(routineId: String, archivedOn: LocalDate)

    suspend fun completeRoutine(
        routineId: String,
        scheduledDate: LocalDate,
        completedAtEpochMillis: Long,
    ): CompletionResult

    suspend fun cancelCompletion(routineId: String, scheduledDate: LocalDate): Boolean
}

enum class CompletionResult {
    COMPLETED,
    ALREADY_COMPLETED,
    FUTURE_DATE,
    ROUTINE_NOT_FOUND,
    NOT_SCHEDULED,
}
