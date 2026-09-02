package com.routina.app.data

import androidx.room.withTransaction
import com.routina.app.data.local.ProfileEntity
import com.routina.app.data.local.RoutineCompletionEntity
import com.routina.app.data.local.RoutineEntity
import com.routina.app.data.local.RoutinaDatabase
import com.routina.app.domain.model.Frequency
import com.routina.app.domain.model.Profile
import com.routina.app.domain.model.Routine
import com.routina.app.domain.model.RoutineCompletion
import com.routina.app.domain.repository.CompletionResult
import com.routina.app.domain.repository.RoutineRepository
import com.routina.app.domain.schedule.RecurrenceCalculator
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoutineRepositoryImpl(
    private val database: RoutinaDatabase,
    private val todayProvider: () -> LocalDate = LocalDate::now,
) : RoutineRepository {
    private val routineDao = database.routineDao()
    private val completionDao = database.routineCompletionDao()
    private val profileDao = database.profileDao()

    override fun observeRoutines(): Flow<List<Routine>> =
        routineDao.observeAll().map { routines -> routines.map(RoutineEntity::toDomain) }

    override fun observeCompletions(from: LocalDate, to: LocalDate): Flow<List<RoutineCompletion>> =
        completionDao.observeBetween(from.toEpochDay(), to.toEpochDay())
            .map { completions -> completions.map(RoutineCompletionEntity::toDomain) }

    override fun observeProfile(): Flow<Profile> =
        profileDao.observe().map { profile ->
            profile?.let { Profile(it.totalXp, it.totalPoints) } ?: Profile()
        }

    override suspend fun createRoutine(routine: Routine) {
        routineDao.insert(routine.toEntity())
    }

    override suspend fun archiveRoutine(routineId: String, archivedOn: LocalDate) {
        routineDao.archive(routineId, archivedOn.toEpochDay())
    }

    override suspend fun completeRoutine(
        routineId: String,
        scheduledDate: LocalDate,
        completedAtEpochMillis: Long,
    ): CompletionResult = database.withTransaction {
        if (scheduledDate > todayProvider()) {
            return@withTransaction CompletionResult.FUTURE_DATE
        }
        val routine = routineDao.findById(routineId)?.toDomain() ?: return@withTransaction CompletionResult.ROUTINE_NOT_FOUND
        if (!RecurrenceCalculator.isScheduledOn(routine, scheduledDate)) {
            return@withTransaction CompletionResult.NOT_SCHEDULED
        }

        val completion = RoutineCompletionEntity(
            routineId = routine.id,
            scheduledEpochDay = scheduledDate.toEpochDay(),
            completedAtEpochMillis = completedAtEpochMillis,
            awardedXp = routine.rewardXp,
            awardedPoints = routine.rewardPoints,
        )
        if (completionDao.insertIgnore(completion) == -1L) {
            return@withTransaction CompletionResult.ALREADY_COMPLETED
        }

        profileDao.insertIgnore(ProfileEntity())
        profileDao.addRewards(completion.awardedXp, completion.awardedPoints)
        CompletionResult.COMPLETED
    }

    override suspend fun cancelCompletion(routineId: String, scheduledDate: LocalDate): Boolean =
        database.withTransaction {
            val completion = completionDao.find(routineId, scheduledDate.toEpochDay())
                ?: return@withTransaction false
            if (completionDao.delete(routineId, scheduledDate.toEpochDay()) == 0) {
                return@withTransaction false
            }

            profileDao.insertIgnore(ProfileEntity())
            profileDao.removeRewards(completion.awardedXp, completion.awardedPoints)
            true
        }
}

private fun RoutineEntity.toDomain(): Routine = Routine(
    id = id,
    name = name,
    startDate = LocalDate.ofEpochDay(startEpochDay),
    frequency = Frequency.EveryDays(intervalDays),
    rewardXp = rewardXp,
    rewardPoints = rewardPoints,
    createdAtEpochMillis = createdAtEpochMillis,
    archivedEpochDay = archivedEpochDay,
)

private fun Routine.toEntity(): RoutineEntity = RoutineEntity(
    id = id,
    name = name,
    startEpochDay = startDate.toEpochDay(),
    intervalDays = frequency.intervalDays,
    rewardXp = rewardXp,
    rewardPoints = rewardPoints,
    createdAtEpochMillis = createdAtEpochMillis,
    archivedEpochDay = archivedEpochDay,
)

private fun RoutineCompletionEntity.toDomain(): RoutineCompletion = RoutineCompletion(
    routineId = routineId,
    scheduledDate = LocalDate.ofEpochDay(scheduledEpochDay),
    completedAtEpochMillis = completedAtEpochMillis,
    awardedXp = awardedXp,
    awardedPoints = awardedPoints,
)
