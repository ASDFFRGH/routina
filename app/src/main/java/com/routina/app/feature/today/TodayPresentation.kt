package com.routina.app.feature.today

import com.routina.app.domain.model.Routine
import com.routina.app.domain.model.RoutineCompletion
import com.routina.app.domain.schedule.RecurrenceCalculator
import java.time.LocalDate

/** A routine scheduled for the displayed day. The date travels with actions to avoid stale taps. */
data class TodayRoutine(
    val routine: Routine,
    val scheduledDate: LocalDate,
    val isCompleted: Boolean,
)

data class TodayUiState(
    val date: LocalDate,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isProcessing: Boolean = false,
    val pending: List<TodayRoutine> = emptyList(),
    val completed: List<TodayRoutine> = emptyList(),
) {
    val scheduledCount: Int get() = pending.size + completed.size
    val completedCount: Int get() = completed.size
    val nextRoutine: TodayRoutine? get() = pending.firstOrNull()
    val isEmpty: Boolean get() = !isLoading && errorMessage == null && scheduledCount == 0
    val isAllDone: Boolean get() = !isLoading && errorMessage == null && scheduledCount > 0 && pending.isEmpty()

    companion object {
        fun loading(date: LocalDate) = TodayUiState(date = date, isLoading = true)
        fun error(date: LocalDate, message: String) = TodayUiState(date = date, errorMessage = message)
    }
}

/** Pure day projection used by the screen and unit tests. */
fun todayUiState(
    date: LocalDate,
    routines: List<Routine>,
    completions: List<RoutineCompletion>,
): TodayUiState {
    val completedIds = completions.asSequence()
        .filter { it.scheduledDate == date }
        .map { it.routineId }
        .toSet()
    val scheduled = routines.asSequence()
        .filter { RecurrenceCalculator.isScheduledOn(it, date) }
        .sortedWith(compareBy<Routine> { it.createdAtEpochMillis }.thenBy { it.id })
        .map { TodayRoutine(it, date, it.id in completedIds) }
        .toList()
    return TodayUiState(
        date = date,
        pending = scheduled.filterNot(TodayRoutine::isCompleted),
        completed = scheduled.filter(TodayRoutine::isCompleted),
    )
}
