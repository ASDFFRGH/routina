@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.routina.app.feature.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.routina.app.domain.repository.RoutineRepository
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun CalendarRoute(
    repository: RoutineRepository,
    onAddRoutine: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = viewModel(factory = CalendarViewModelFactory(repository)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshToday()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(lifecycleOwner, viewModel) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.refreshToday()
            while (isActive) {
                delay(millisUntilNextLocalDateBoundary(ZonedDateTime.now()))
                viewModel.refreshToday()
            }
        }
    }
    CalendarScreen(
        uiState = uiState,
        onPreviousMonth = viewModel::previousMonth,
        onNextMonth = viewModel::nextMonth,
        onSelectDate = viewModel::selectDate,
        onToggleCompletion = viewModel::toggleCompletion,
        onAddRoutine = onAddRoutine,
        modifier = modifier,
    )
}

@Composable
fun CalendarScreen(
    uiState: CalendarUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    onToggleCompletion: (ScheduledRoutine) -> Unit,
    onAddRoutine: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("カレンダー") }) },
        floatingActionButton = { FloatingActionButton(onClick = onAddRoutine) { Text("追加") } },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onPreviousMonth, modifier = Modifier.semantics { contentDescription = "前の月" }) { Text("‹", style = MaterialTheme.typography.headlineMedium) }
                    Text("${uiState.displayedMonth.year}年${uiState.displayedMonth.monthValue}月", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onNextMonth, modifier = Modifier.semantics { contentDescription = "次の月" }) { Text("›", style = MaterialTheme.typography.headlineMedium) }
                }
            }
            item { MonthGrid(uiState.days, uiState.selectedDate, uiState.today, onSelectDate) }
            item {
                Text("${uiState.selectedDate.format(DateTimeFormatter.ofPattern("M月d日（E）", Locale.JAPANESE))}のルーティーン", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            item {
                DayRoutineList(
                    routines = uiState.selectedRoutines,
                    canEdit = uiState.selectedDate <= uiState.today,
                    onToggleCompletion = onToggleCompletion,
                    onAddRoutine = onAddRoutine,
                )
            }
        }
    }
}

@Composable
fun MonthGrid(
    days: List<CalendarDay>,
    selectedDate: LocalDate,
    today: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            listOf("月", "火", "水", "木", "金", "土", "日").forEach { weekday ->
                Text(weekday, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
            }
        }
        days.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    DayCell(day, day.date == selectedDate, day.date == today, onSelectDate, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: CalendarDay,
    isSelected: Boolean,
    isToday: Boolean,
    onSelectDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val background = when {
        isSelected -> colors.primaryContainer
        isToday -> colors.secondaryContainer
        else -> Color.Transparent
    }
    val textColor = if (day.isCurrentMonth) colors.onSurface else colors.onSurface.copy(alpha = .35f)
    Column(
        modifier = modifier
            .height(52.dp)
            .padding(2.dp)
            .background(background, CircleShape)
            .semantics {
                selected = isSelected
                contentDescription = listOfNotNull(
                    day.date.toString(),
                    if (isToday) "今日" else null,
                    if (isSelected) "選択中" else null,
                    day.status.label,
                    "${day.completedCount}/${day.scheduledCount}件完了",
                ).joinToString("、")
            }
            .then(if (day.isCurrentMonth) Modifier.clickable { onSelectDate(day.date) } else Modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(day.date.dayOfMonth.toString(), color = textColor, style = MaterialTheme.typography.bodyMedium)
        Text(day.status.symbol, color = statusColor(day.status, colors.primary, colors.error, colors.tertiary), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DayRoutineList(
    routines: List<ScheduledRoutine>,
    canEdit: Boolean,
    onToggleCompletion: (ScheduledRoutine) -> Unit,
    onAddRoutine: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (routines.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("この日の予定はありません")
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onAddRoutine) { Text("ルーティーンを追加") }
                }
            }
        } else {
            routines.forEach { item ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(checked = item.isCompleted, onCheckedChange = { onToggleCompletion(item) }, enabled = canEdit)
                        Spacer(Modifier.width(4.dp))
                        Column {
                            Text(item.routine.name, style = MaterialTheme.typography.titleSmall)
                            Text(if (canEdit) "完了を記録できます" else "未来の予定は記録できません", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

/** Returns the delay until the next local date begins for the supplied instant and zone. */
internal fun millisUntilNextLocalDateBoundary(now: ZonedDateTime): Long {
    val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(now.zone)
    return Duration.between(now, nextMidnight).toMillis().coerceAtLeast(1)
}

private fun statusColor(status: DayStatus, primary: Color, error: Color, tertiary: Color): Color = when (status) {
    DayStatus.COMPLETE -> primary
    DayStatus.MISSED -> error
    DayStatus.PARTIAL -> tertiary
    DayStatus.PENDING, DayStatus.SCHEDULED -> tertiary
    DayStatus.NO_SCHEDULE -> Color.Gray
}
