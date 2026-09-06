package com.routina.app.feature.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.routina.app.domain.repository.RoutineRepository
import com.routina.app.domain.repository.CompletionResult
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class TodayViewModel(
    private val repository: RoutineRepository,
    private val todayProvider: () -> LocalDate = LocalDate::now,
    private val clock: () -> Long = System::currentTimeMillis,
) : ViewModel() {
    private val currentToday = MutableStateFlow(todayProvider())
    private val refreshVersion = MutableStateFlow(0)
    private val actionError = MutableStateFlow<String?>(null)
    private val isProcessing = MutableStateFlow(false)

    private val content = combine(currentToday, refreshVersion) { date, _ -> date }
        .flatMapLatest { date ->
            combine(repository.observeRoutines(), repository.observeCompletions(date, date)) { routines, completions ->
                todayUiState(date, routines, completions)
            }.onStart {
                emit(TodayUiState.loading(date))
            }.catch { error ->
                if (error is CancellationException) throw error
                emit(TodayUiState.error(date, "今日の予定を読み込めませんでした。"))
            }
        }

    val uiState = combine(content, actionError, isProcessing) { state, error, processing ->
        state.copy(errorMessage = error ?: state.errorMessage, isProcessing = processing)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        TodayUiState.loading(currentToday.value),
    )

    fun complete(item: TodayRoutine) = update(item, complete = true)

    fun cancel(item: TodayRoutine) = update(item, complete = false)

    fun refreshToday() {
        val newToday = todayProvider()
        if (newToday != currentToday.value) {
            actionError.value = null
            currentToday.value = newToday
        }
    }

    fun retry() {
        actionError.value = null
        refreshVersion.value += 1
    }

    private fun update(item: TodayRoutine, complete: Boolean) {
        if (isProcessing.value) return
        isProcessing.value = true
        actionError.value = null
        viewModelScope.launch {
            try {
                // A tap rendered for yesterday must never mutate today's record.
                if (item.scheduledDate != todayProvider()) {
                    refreshToday()
                    return@launch
                }
                if (complete) {
                    when (repository.completeRoutine(item.routine.id, item.scheduledDate, clock())) {
                        CompletionResult.COMPLETED, CompletionResult.ALREADY_COMPLETED -> Unit
                        else -> actionError.value = "完了を記録できませんでした。もう一度お試しください。"
                    }
                } else {
                    // A missing completion is already the desired end state (for example, after a fast second tap).
                    repository.cancelCompletion(item.routine.id, item.scheduledDate)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                actionError.value = if (complete) "完了を記録できませんでした。もう一度お試しください。" else "完了の取消ができませんでした。もう一度お試しください。"
            } finally {
                isProcessing.value = false
            }
        }
    }
}

class TodayViewModelFactory(
    private val repository: RoutineRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = TodayViewModel(repository) as T
}
