@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.routina.app.feature.routines

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.routina.app.domain.model.Routine
import com.routina.app.domain.repository.RoutineRepository
import java.time.Instant
import java.time.LocalDate
import java.time.Duration
import java.time.ZonedDateTime
import java.time.ZoneOffset
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun RoutineListRoute(
    repository: RoutineRepository,
    onAddRoutine: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RoutineListViewModel = viewModel(factory = RoutineListViewModelFactory(repository)),
) {
    val routines by viewModel.routines.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshToday()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(viewModel) {
        while (isActive) {
            viewModel.refreshToday()
            val now = ZonedDateTime.now()
            delay(Duration.between(now, now.toLocalDate().plusDays(1).atStartOfDay(now.zone)).toMillis().coerceAtLeast(1))
        }
    }
    RoutineListScreen(routines, onAddRoutine, viewModel::archive, viewModel::reorder, modifier)
}

@Composable
fun RoutineListScreen(
    routines: List<RoutineListItem>,
    onAddRoutine: () -> Unit,
    onArchive: (Routine) -> Unit,
    onReorder: (List<RoutineListItem>) -> Unit,
    modifier: Modifier = Modifier,
) {
    var ordered by remember(routines) { mutableStateOf(routines.sortedWith(compareBy<RoutineListItem> { !it.routine.isActive() }.thenBy { it.routine.sortOrder })) }
    var draggingId by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableStateOf(0f) }
    var dragStartingOrder by remember { mutableStateOf<List<RoutineListItem>?>(null) }
    val listState = rememberLazyListState()
    val edgeScrollThreshold = with(LocalDensity.current) { 72.dp.toPx() }
    val currentOnReorder by rememberUpdatedState(onReorder)

    fun move(id: String, direction: Int): Boolean {
        val from = ordered.indexOfFirst { it.routine.id == id }
        val destination = from + direction
        if (from >= 0 && destination in ordered.indices && ordered[destination].routine.isActive()) {
            ordered = ordered.toMutableList().also { list ->
                val moved = list.removeAt(from)
                list.add(destination, moved)
            }
            return true
        }
        return false
    }
    fun applyDragDelta(id: String, delta: Float, rowHeight: Float) {
        dragOffset += delta
        while (dragOffset >= rowHeight) { move(id, 1); dragOffset -= rowHeight }
        while (dragOffset <= -rowHeight) { move(id, -1); dragOffset += rowHeight }
    }

    // Keep the dragged row under the pointer while it is held near a viewport edge.
    // The reordered item remains keyed by id, so the gesture continues after it changes index.
    LaunchedEffect(draggingId) {
        while (isActive && draggingId != null) {
            val id = draggingId ?: break
            val layout = listState.layoutInfo
            val item = layout.visibleItemsInfo.firstOrNull { it.key == id }
            if (item != null) {
                val top = item.offset + dragOffset
                val bottom = top + item.size
                val scrollBy = when {
                    top < layout.viewportStartOffset + edgeScrollThreshold ->
                        (top - (layout.viewportStartOffset + edgeScrollThreshold)).coerceAtLeast(-edgeScrollThreshold)
                    bottom > layout.viewportEndOffset - edgeScrollThreshold ->
                        (bottom - (layout.viewportEndOffset - edgeScrollThreshold)).coerceAtMost(edgeScrollThreshold)
                    else -> 0f
                }
                if (scrollBy != 0f) {
                    val consumed = listState.scrollBy(scrollBy)
                    applyDragDelta(id, consumed, item.size.coerceAtLeast(1).toFloat())
                }
            }
            delay(16)
        }
    }
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("ルーティーン") }) },
        floatingActionButton = { FloatingActionButton(onClick = onAddRoutine) { Text("追加") } },
    ) { padding ->
        if (ordered.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text("ルーティーンはまだありません", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("追加して、毎日の習慣を育てましょう。")
                Spacer(Modifier.height(16.dp))
                Button(onClick = onAddRoutine) { Text("ルーティーンを追加") }
            }
        } else {
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(padding)) {
                items(ordered, key = { it.routine.id }) { item ->
                    Column(modifier = if (draggingId == item.routine.id) Modifier else Modifier.animateItem()) {
                        RoutineRow(
                            item = item,
                            onArchive = onArchive,
                            isDragging = draggingId == item.routine.id,
                            dragOffset = if (draggingId == item.routine.id) dragOffset else 0f,
                            onDragStart = {
                                draggingId = item.routine.id
                                dragOffset = 0f
                                dragStartingOrder = ordered
                            },
                            onDragDelta = { delta, rowHeight ->
                                applyDragDelta(item.routine.id, delta, rowHeight)
                            },
                            onMoveByAccessibility = { direction ->
                                move(item.routine.id, direction).also { moved ->
                                    if (moved) currentOnReorder(ordered)
                                }
                            },
                            onDragEnd = { commit ->
                                if (commit) {
                                    currentOnReorder(ordered)
                                } else {
                                    dragStartingOrder?.let { ordered = it }
                                }
                                draggingId = null
                                dragOffset = 0f
                                dragStartingOrder = null
                            },
                        )
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
private fun RoutineRow(
    item: RoutineListItem,
    onArchive: (Routine) -> Unit,
    isDragging: Boolean,
    dragOffset: Float,
    onDragStart: () -> Unit,
    onDragDelta: (delta: Float, rowHeight: Float) -> Unit,
    onDragEnd: (commit: Boolean) -> Unit,
    onMoveByAccessibility: (Int) -> Boolean,
) {
    val routine = item.routine
    var rowHeight by remember(routine.id) { mutableStateOf(1f) }
    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDragDelta by rememberUpdatedState(onDragDelta)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)
    val currentOnMoveByAccessibility by rememberUpdatedState(onMoveByAccessibility)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged { rowHeight = it.height.coerceAtLeast(1).toFloat() }
            .graphicsLayer { translationY = dragOffset }
            .zIndex(if (isDragging) 1f else 0f)
            .shadow(if (isDragging) 8.dp else 0.dp)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(routine.name, style = MaterialTheme.typography.titleMedium)
            Text("${routine.frequency.displayName()} ・ 開始 ${routine.startDate}")
            Text("連続継続日数: ${item.streak}日（予定日）")
            Text("報酬: ${routine.rewardXp} XP / ${routine.rewardPoints} ポイント")
            if (!routine.isActive()) Text("アーカイブ済み")
        }
        if (routine.isActive()) {
            Text(
                "☰",
                modifier = Modifier
                    .size(48.dp)
                    .semantics {
                        contentDescription = "${routine.name}を並び替え"
                        customActions = listOf(
                            CustomAccessibilityAction("上に移動") { currentOnMoveByAccessibility(-1) },
                            CustomAccessibilityAction("下に移動") { currentOnMoveByAccessibility(1) },
                        )
                    }
                    .pointerInput(routine.id) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { currentOnDragStart() },
                            onDragCancel = { currentOnDragEnd(false) },
                            onDragEnd = { currentOnDragEnd(true) },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                currentOnDragDelta(dragAmount.y, rowHeight)
                            },
                        )
                    }
                    .wrapContentSize(Alignment.Center),
            )
            Spacer(Modifier.width(8.dp))
            TextButton(onClick = { onArchive(routine) }) { Text("アーカイブ") }
        }
    }
}

@Composable
fun RoutineFormRoute(
    repository: RoutineRepository,
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RoutineFormViewModel = viewModel(factory = RoutineFormViewModelFactory(repository)),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) { viewModel.saved.collect { onSaved() } }
    RoutineFormScreen(state, viewModel::updateName, viewModel::updateStartDate, viewModel::updatePreset, viewModel::updateCustomInterval, viewModel::save, onCancel, modifier)
}

@Composable
fun RoutineFormScreen(
    state: RoutineFormState,
    onNameChanged: (String) -> Unit,
    onStartDateChanged: (LocalDate) -> Unit,
    onPresetChanged: (FrequencyPreset) -> Unit,
    onCustomIntervalChanged: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    Scaffold(modifier = modifier, topBar = { TopAppBar(title = { Text("ルーティーンを登録") }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(state.name, onNameChanged, Modifier.fillMaxWidth(), label = { Text("名前") }, singleLine = true, isError = state.name.isNotEmpty() && state.name.isBlank())
            OutlinedTextField(state.startDate.toString(), {}, Modifier.fillMaxWidth(), label = { Text("開始日") }, readOnly = true, trailingIcon = { TextButton(onClick = { showDatePicker = true }) { Text("変更") } })
            FrequencySelector(state.preset, onPresetChanged)
            if (state.preset == FrequencyPreset.CUSTOM) {
                OutlinedTextField(state.customInterval, onCustomIntervalChanged, Modifier.fillMaxWidth(), label = { Text("何日ごと") }, suffix = { Text("日") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), isError = state.customInterval.isNotEmpty() && state.customInterval.toLongOrNull()?.let { it < 1 } != false)
            }
            Text("完了すると 20 XP と 10 ポイントを獲得します。")
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onCancel, enabled = !state.isSaving) { Text("キャンセル") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = onSave, enabled = state.isValid && !state.isSaving) { Text(if (state.isSaving) "保存中…" else "保存") }
            }
        }
    }
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = state.startDate.toEpochDay() * 86_400_000L)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { datePickerState.selectedDateMillis?.let { onStartDateChanged(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()) }; showDatePicker = false }) { Text("決定") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("キャンセル") } },
        ) { DatePicker(datePickerState) }
    }
}

@Composable
private fun FrequencySelector(selected: FrequencyPreset, onSelected: (FrequencyPreset) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.label(), onValueChange = {}, readOnly = true, label = { Text("頻度") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            FrequencyPreset.entries.forEach { preset ->
                DropdownMenuItem(text = { Text(preset.label()) }, onClick = { onSelected(preset); expanded = false })
            }
        }
    }
}

private fun FrequencyPreset.label(): String = when (this) {
    FrequencyPreset.DAILY -> "毎日"
    FrequencyPreset.EVERY_THREE_DAYS -> "3日ごと"
    FrequencyPreset.WEEKLY -> "週1回"
    FrequencyPreset.CUSTOM -> "任意日数"
}
