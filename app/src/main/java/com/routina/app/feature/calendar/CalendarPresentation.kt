package com.routina.app.feature.calendar

import com.routina.app.domain.model.Routine
import com.routina.app.domain.model.RoutineCompletion
import com.routina.app.domain.schedule.RecurrenceCalculator
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

enum class DayStatus(val label: String, val symbol: String) {
    NO_SCHEDULE("予定なし", "・"),
    MISSED("未達成", "×"),
    PENDING("未実行", "○"),
    SCHEDULED("予定", "○"),
    PARTIAL("一部完了", "△"),
    COMPLETE("完了", "✓"),
}

data class CalendarDay(
    val date: LocalDate,
    val isCurrentMonth: Boolean,
    val scheduledCount: Int,
    val completedCount: Int,
    val status: DayStatus,
)

data class ScheduledRoutine(
    val routine: Routine,
    val isCompleted: Boolean,
)

data class CalendarUiState(
    val displayedMonth: YearMonth,
    val selectedDate: LocalDate,
    val today: LocalDate,
    val days: List<CalendarDay> = emptyList(),
    val selectedRoutines: List<ScheduledRoutine> = emptyList(),
)

/** Monday-first calendar calculations kept free of Android and Compose dependencies. */
fun monthDays(
    month: YearMonth,
    routines: List<Routine>,
    completions: List<RoutineCompletion>,
    today: LocalDate,
): List<CalendarDay> {
    val gridRange = monthGridRange(month)
    return generateSequence(gridRange.start) { date ->
        date.takeIf { it < gridRange.endInclusive }?.plusDays(1)
    }.map { date ->
        val scheduled = routines.filter { RecurrenceCalculator.isScheduledOn(it, date) }
        val completedIds = completions.asSequence()
            .filter { it.scheduledDate == date }
            .map { it.routineId }
            .toSet()
        val completedCount = scheduled.count { it.id in completedIds }
        CalendarDay(
            date = date,
            isCurrentMonth = YearMonth.from(date) == month,
            scheduledCount = scheduled.size,
            completedCount = completedCount,
            status = dayStatus(date, scheduled.size, completedCount, today),
        )
    }.toList()
}

/** The Monday-first, Sunday-last range rendered by a calendar month grid. */
fun monthGridRange(month: YearMonth): ClosedRange<LocalDate> {
    val first = month.atDay(1)
    val start = first.minusDays((first.dayOfWeek.value - DayOfWeek.MONDAY.value).toLong())
    val last = month.atEndOfMonth()
    val trailingDays = DayOfWeek.SUNDAY.value - last.dayOfWeek.value
    return start..last.plusDays(trailingDays.toLong())
}

fun scheduledRoutinesFor(
    date: LocalDate,
    routines: List<Routine>,
    completions: List<RoutineCompletion>,
): List<ScheduledRoutine> {
    val completedIds = completions.asSequence()
        .filter { it.scheduledDate == date }
        .map { it.routineId }
        .toSet()
    return routines.filter { RecurrenceCalculator.isScheduledOn(it, date) }
        .map { ScheduledRoutine(it, it.id in completedIds) }
}

fun dayStatus(date: LocalDate, scheduledCount: Int, completedCount: Int, today: LocalDate): DayStatus = when {
    scheduledCount == 0 -> DayStatus.NO_SCHEDULE
    completedCount == scheduledCount -> DayStatus.COMPLETE
    completedCount > 0 -> DayStatus.PARTIAL
    date < today -> DayStatus.MISSED
    date == today -> DayStatus.PENDING
    else -> DayStatus.SCHEDULED
}

fun selectionForMonth(month: YearMonth, today: LocalDate): LocalDate =
    if (YearMonth.from(today) == month) today else month.atDay(1)
