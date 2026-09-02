package com.routina.app.feature.calendar

import com.routina.app.domain.model.Frequency
import com.routina.app.domain.model.Routine
import com.routina.app.domain.model.RoutineCompletion
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalendarPresentationTest {
    private val today = LocalDate.of(2026, 9, 3)

    @Test
    fun `month grid starts on monday and includes all month lengths`() {
        val february28 = monthDays(YearMonth.of(2021, 2), emptyList(), emptyList(), today)
        val february29 = monthDays(YearMonth.of(2024, 2), emptyList(), emptyList(), today)
        val april30 = monthDays(YearMonth.of(2026, 4), emptyList(), emptyList(), today)
        val august31 = monthDays(YearMonth.of(2026, 8), emptyList(), emptyList(), today)

        assertEquals(LocalDate.of(2021, 2, 1), february28.first().date)
        assertEquals(28, february28.size)
        assertEquals(29, february29.count { it.isCurrentMonth })
        assertEquals(30, april30.count { it.isCurrentMonth })
        assertEquals(31, august31.count { it.isCurrentMonth })
        assertTrue(august31.size % 7 == 0)
        assertEquals(java.time.DayOfWeek.MONDAY, august31.first().date.dayOfWeek)
    }

    @Test
    fun `month grid range includes leading and trailing gray cells`() {
        val range = monthGridRange(YearMonth.of(2026, 8))

        assertEquals(LocalDate.of(2026, 7, 27), range.start)
        assertEquals(LocalDate.of(2026, 9, 6), range.endInclusive)
    }

    @Test
    fun `daily schedules aggregate no schedule pending partial and complete`() {
        val date = LocalDate.of(2026, 9, 3)
        val first = routine("one", date)
        val second = routine("two", date)
        val completions = listOf(completion("one", date))
        val day = monthDays(YearMonth.from(date), listOf(first, second), completions, date)
            .first { it.date == date }

        assertEquals(2, day.scheduledCount)
        assertEquals(1, day.completedCount)
        assertEquals(DayStatus.PARTIAL, day.status)
        assertEquals(DayStatus.NO_SCHEDULE, dayStatus(date, 0, 0, date))
        assertEquals(DayStatus.COMPLETE, dayStatus(date, 2, 2, date))
    }

    @Test
    fun `incomplete status distinguishes past today and future`() {
        assertEquals(DayStatus.MISSED, dayStatus(today.minusDays(1), 1, 0, today))
        assertEquals(DayStatus.PENDING, dayStatus(today, 1, 0, today))
        assertEquals(DayStatus.SCHEDULED, dayStatus(today.plusDays(1), 1, 0, today))
    }

    @Test
    fun `scheduled routine list respects recurrence and completion`() {
        val start = LocalDate.of(2026, 9, 1)
        val routine = routine("three-days", start, Frequency.EveryThreeDays)

        val due = scheduledRoutinesFor(start.plusDays(3), listOf(routine), listOf(completion(routine.id, start.plusDays(3))) )
        val notDue = scheduledRoutinesFor(start.plusDays(1), listOf(routine), emptyList())

        assertEquals(1, due.size)
        assertTrue(due.single().isCompleted)
        assertTrue(notDue.isEmpty())
    }

    @Test
    fun `same day completion remains visible when archive takes effect tomorrow`() {
        val date = LocalDate.of(2026, 9, 3)
        val routine = routine("archived", date).copy(archivedEpochDay = date.plusDays(1).toEpochDay())
        val completions = listOf(completion(routine.id, date))

        val scheduled = scheduledRoutinesFor(date, listOf(routine), completions)
        val day = monthDays(YearMonth.from(date), listOf(routine), completions, date)
            .first { it.date == date }

        assertEquals(1, scheduled.size)
        assertTrue(scheduled.single().isCompleted)
        assertEquals(1, day.scheduledCount)
        assertEquals(1, day.completedCount)
        assertEquals(DayStatus.COMPLETE, day.status)
    }

    @Test
    fun `month navigation selection is today only in todays month`() {
        assertEquals(today, selectionForMonth(YearMonth.from(today), today))
        assertEquals(LocalDate.of(2026, 8, 1), selectionForMonth(YearMonth.of(2026, 8), today))
        assertFalse(selectionForMonth(YearMonth.of(2026, 10), today) > YearMonth.of(2026, 10).atEndOfMonth())
    }

    @Test
    fun `today refresh follows a selected today across an ordinary day boundary`() {
        val previousToday = LocalDate.of(2026, 9, 3)
        val newToday = previousToday.plusDays(1)

        val position = calendarPositionAfterTodayRefresh(
            displayedMonth = YearMonth.from(previousToday),
            selectedDate = previousToday,
            previousToday = previousToday,
            newToday = newToday,
        )

        assertEquals(YearMonth.from(newToday), position.displayedMonth)
        assertEquals(newToday, position.selectedDate)
    }

    @Test
    fun `today refresh follows a selected today into the next month`() {
        val previousToday = LocalDate.of(2026, 9, 30)
        val newToday = previousToday.plusDays(1)

        val position = calendarPositionAfterTodayRefresh(
            displayedMonth = YearMonth.from(previousToday),
            selectedDate = previousToday,
            previousToday = previousToday,
            newToday = newToday,
        )

        assertEquals(YearMonth.of(2026, 10), position.displayedMonth)
        assertEquals(newToday, position.selectedDate)
    }

    @Test
    fun `today refresh preserves a different selected day in the current month`() {
        val previousToday = LocalDate.of(2026, 9, 3)
        val selectedDate = LocalDate.of(2026, 9, 1)

        val position = calendarPositionAfterTodayRefresh(
            displayedMonth = YearMonth.from(previousToday),
            selectedDate = selectedDate,
            previousToday = previousToday,
            newToday = previousToday.plusDays(1),
        )

        assertEquals(YearMonth.from(previousToday), position.displayedMonth)
        assertEquals(selectedDate, position.selectedDate)
    }

    @Test
    fun `today refresh preserves a past month being viewed`() {
        val previousToday = LocalDate.of(2026, 9, 3)
        val displayedMonth = YearMonth.of(2026, 8)
        val selectedDate = LocalDate.of(2026, 8, 15)

        val position = calendarPositionAfterTodayRefresh(
            displayedMonth = displayedMonth,
            selectedDate = selectedDate,
            previousToday = previousToday,
            newToday = previousToday.plusDays(1),
        )

        assertEquals(displayedMonth, position.displayedMonth)
        assertEquals(selectedDate, position.selectedDate)
    }

    @Test
    fun `delay until next local date boundary is one minute just before midnight`() {
        val now = ZonedDateTime.of(2026, 9, 3, 23, 59, 0, 0, ZoneId.of("Asia/Tokyo"))

        assertEquals(60_000L, millisUntilNextLocalDateBoundary(now))
    }

    @Test
    fun `delay until next local date boundary covers the remaining day at ordinary time`() {
        val now = ZonedDateTime.of(2026, 9, 3, 10, 15, 30, 0, ZoneId.of("Asia/Tokyo"))

        assertEquals((13 * 60 * 60 + 44 * 60 + 30) * 1_000L, millisUntilNextLocalDateBoundary(now))
    }

    private fun routine(id: String, start: LocalDate, frequency: Frequency = Frequency.Daily) = Routine(
        id = id,
        name = id,
        startDate = start,
        frequency = frequency,
        rewardXp = 20,
        rewardPoints = 10,
        createdAtEpochMillis = 0,
    )

    private fun completion(id: String, date: LocalDate) = RoutineCompletion(id, date, 0, 20, 10)
}
