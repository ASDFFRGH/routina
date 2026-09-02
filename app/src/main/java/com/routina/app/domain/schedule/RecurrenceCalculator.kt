package com.routina.app.domain.schedule

import com.routina.app.domain.model.Routine
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object RecurrenceCalculator {
    /** Returns whether [date] is scheduled, excluding the archive date and all later dates. */
    fun isScheduledOn(routine: Routine, date: LocalDate): Boolean {
        if (date < routine.startDate) return false
        if (routine.archivedEpochDay?.let(date.toEpochDay()::compareTo)?.let { it >= 0 } == true) {
            return false
        }

        val elapsedDays = ChronoUnit.DAYS.between(routine.startDate, date)
        return elapsedDays % routine.frequency.intervalDays == 0L
    }
}
