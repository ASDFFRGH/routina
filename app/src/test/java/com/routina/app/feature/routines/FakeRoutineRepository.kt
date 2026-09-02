package com.routina.app.feature.routines

import com.routina.app.domain.model.Profile
import com.routina.app.domain.model.Routine
import com.routina.app.domain.model.RoutineCompletion
import com.routina.app.domain.repository.CompletionResult
import com.routina.app.domain.repository.RoutineRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class FakeRoutineRepository : RoutineRepository {
    var archivedRoutineId: String? = null
    var archivedOn: LocalDate? = null
    override fun observeRoutines(): Flow<List<Routine>> = flowOf(emptyList())
    override fun observeCompletions(from: LocalDate, to: LocalDate): Flow<List<RoutineCompletion>> = flowOf(emptyList())
    override fun observeProfile(): Flow<Profile> = flowOf(Profile())
    override suspend fun createRoutine(routine: Routine) = Unit
    override suspend fun archiveRoutine(routineId: String, archivedOn: LocalDate) {
        this.archivedRoutineId = routineId
        this.archivedOn = archivedOn
    }
    override suspend fun completeRoutine(routineId: String, scheduledDate: LocalDate, completedAtEpochMillis: Long) = CompletionResult.COMPLETED
    override suspend fun cancelCompletion(routineId: String, scheduledDate: LocalDate) = false
}
