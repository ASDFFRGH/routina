package com.routina.app.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.routina.app.domain.repository.RoutineRepository
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class CalendarViewModel(
    private val repository: RoutineRepository,
    private val todayProvider: () -> LocalDate = LocalDate::now,
    private val clock: () -> Long = System::currentTimeMillis,
) : ViewModel() {
    private val currentToday = MutableStateFlow(todayProvider())
    private val displayedMonth = MutableStateFlow(YearMonth.from(currentToday.value))
    private val selectedDate = MutableStateFlow(currentToday.value)

    val uiState = combine(displayedMonth, selectedDate, currentToday, repository.observeRoutines()) { month, selected, today, routines ->
        CalendarInputs(month, selected, today, routines)
    }.flatMapLatest { inputs ->
        val gridRange = monthGridRange(inputs.month)
        repository.observeCompletions(gridRange.start, gridRange.endInclusive).map { completions ->
            CalendarUiState(
                displayedMonth = inputs.month,
                selectedDate = inputs.selectedDate,
                today = inputs.today,
                days = monthDays(inputs.month, inputs.routines, completions, inputs.today),
                selectedRoutines = scheduledRoutinesFor(inputs.selectedDate, inputs.routines, completions),
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        CalendarUiState(YearMonth.from(currentToday.value), currentToday.value, currentToday.value),
    )

    fun previousMonth() = moveMonthBy(-1)

    fun nextMonth() = moveMonthBy(1)

    fun selectDate(date: LocalDate) {
        if (YearMonth.from(date) == displayedMonth.value) selectedDate.value = date
    }

    fun toggleCompletion(item: ScheduledRoutine) {
        val date = selectedDate.value
        if (date > todayProvider()) return
        viewModelScope.launch {
            if (item.isCompleted) repository.cancelCompletion(item.routine.id, date)
            else repository.completeRoutine(item.routine.id, date, clock())
        }
    }

    fun refreshToday() {
        val previousToday = currentToday.value
        val newToday = todayProvider()
        if (newToday == previousToday) return

        val refreshedPosition = calendarPositionAfterTodayRefresh(
            displayedMonth = displayedMonth.value,
            selectedDate = selectedDate.value,
            previousToday = previousToday,
            newToday = newToday,
        )
        currentToday.value = newToday
        displayedMonth.value = refreshedPosition.displayedMonth
        selectedDate.value = refreshedPosition.selectedDate
    }

    private fun moveMonthBy(amount: Long) {
        val month = displayedMonth.value.plusMonths(amount)
        displayedMonth.value = month
        selectedDate.value = selectionForMonth(month, currentToday.value)
    }

    private data class CalendarInputs(
        val month: YearMonth,
        val selectedDate: LocalDate,
        val today: LocalDate,
        val routines: List<com.routina.app.domain.model.Routine>,
    )
}

internal data class CalendarPosition(
    val displayedMonth: YearMonth,
    val selectedDate: LocalDate,
)

internal fun calendarPositionAfterTodayRefresh(
    displayedMonth: YearMonth,
    selectedDate: LocalDate,
    previousToday: LocalDate,
    newToday: LocalDate,
): CalendarPosition =
    if (selectedDate == previousToday) {
        CalendarPosition(YearMonth.from(newToday), newToday)
    } else {
        CalendarPosition(displayedMonth, selectedDate)
    }

class CalendarViewModelFactory(
    private val repository: RoutineRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = CalendarViewModel(repository) as T
}
