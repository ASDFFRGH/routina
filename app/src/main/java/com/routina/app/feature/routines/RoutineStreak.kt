package com.routina.app.feature.routines

import com.routina.app.domain.model.Routine
import com.routina.app.domain.model.RoutineCompletion
import com.routina.app.domain.schedule.RecurrenceCalculator
import java.time.LocalDate

/** Consecutive completed scheduled occurrences, ending at the latest required occurrence. */
fun routineStreak(
    routine: Routine,
    completions: Collection<RoutineCompletion>,
    today: LocalDate,
): Int {
    val lastRelevantDate = routine.archivedEpochDay
        ?.let { LocalDate.ofEpochDay(it).minusDays(1).coerceAtMost(today) }
        ?: today
    if (lastRelevantDate < routine.startDate) return 0

    val completedDates = completions.asSequence()
        .filter { it.routineId == routine.id }
        .map { it.scheduledDate }
        .toSet()

    // Today's unchecked occurrence is still actionable, and therefore does not break the streak.
    var date = if (RecurrenceCalculator.isScheduledOn(routine, lastRelevantDate) &&
        lastRelevantDate == today && lastRelevantDate !in completedDates
    ) {
        lastRelevantDate.minusDays(routine.frequency.intervalDays)
    } else {
        lastRelevantDate
    }
    while (date >= routine.startDate && !RecurrenceCalculator.isScheduledOn(routine, date)) {
        date = date.minusDays(1)
    }

    var streak = 0
    while (date >= routine.startDate && RecurrenceCalculator.isScheduledOn(routine, date)) {
        if (date !in completedDates) break
        streak++
        date = date.minusDays(routine.frequency.intervalDays)
    }
    return streak
}

data class RoutineListItem(val routine: Routine, val streak: Int)
