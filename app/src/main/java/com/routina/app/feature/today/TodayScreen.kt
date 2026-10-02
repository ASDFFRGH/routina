@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.routina.app.feature.today

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.zIndex
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.routina.app.domain.repository.RoutineRepository
import com.routina.app.ui.components.RoutinaMascot
import com.routina.app.ui.components.rememberRoutineDragState
import com.routina.app.ui.components.RoutineDragState
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun TodayRoute(
    repository: RoutineRepository,
    onAddRoutine: () -> Unit,
    onOpenRoutines: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TodayViewModel = viewModel(factory = TodayViewModelFactory(repository)),
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
                delay(millisUntilNextTodayBoundary(ZonedDateTime.now()))
                viewModel.refreshToday()
            }
        }
    }
    TodayScreen(
        uiState = uiState,
        onComplete = viewModel::complete,
        onCancel = viewModel::cancel,
        onRetry = viewModel::retry,
        onAddRoutine = onAddRoutine,
        onOpenRoutines = onOpenRoutines,
        modifier = modifier,
        onReorderPending = viewModel::reorderPending,
    )
}

@Composable
fun TodayScreen(
    uiState: TodayUiState,
    onComplete: (TodayRoutine) -> Unit,
    onCancel: (TodayRoutine) -> Unit,
    onRetry: () -> Unit,
    onAddRoutine: () -> Unit,
    onOpenRoutines: () -> Unit,
    modifier: Modifier = Modifier,
    onReorderPending: (List<String>) -> Unit = {},
) {
    var completedExpanded by remember(uiState.date) { mutableStateOf(false) }
    var pendingOrder by remember(uiState.date, uiState.pending) { mutableStateOf(uiState.pending) }
    var dragStartingOrder by remember { mutableStateOf<List<TodayRoutine>?>(null) }
    val listState = rememberLazyListState()
    fun movePending(item: TodayRoutine, direction: Int): Boolean {
        val from = pendingOrder.indexOfFirst { it.routine.id == item.routine.id }
        val target = from + direction
        if (from >= 0 && target in pendingOrder.indices) {
            pendingOrder = pendingOrder.toMutableList().also { it.add(target, it.removeAt(from)) }
            return true
        }
        return false
    }
    val dragState = rememberRoutineDragState(listState, pendingOrder.map { it.routine.id }) { id, targetId ->
        val from = pendingOrder.indexOfFirst { it.routine.id == id }
        val target = pendingOrder.indexOfFirst { it.routine.id == targetId }
        if (from < 0 || target < 0) false else {
            pendingOrder = pendingOrder.toMutableList().also { it.add(target, it.removeAt(from)) }
            true
        }
    }
    LaunchedEffect(uiState.date, uiState.pending) {
        dragState.reset()
        dragStartingOrder = null
    }
    fun finishPendingDrag(commit: Boolean) {
        if (dragStartingOrder == null) return
        if (commit) onReorderPending(pendingOrder.map { it.routine.id })
        else dragStartingOrder?.let { pendingOrder = it }
        dragStartingOrder = null
        dragState.reset()
    }
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("今日") }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddRoutine,
                modifier = Modifier.semantics { contentDescription = "ルーティーンを追加" },
            ) { Text("追加") }
        },
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(uiState.date.format(DateTimeFormatter.ofPattern("M月d日（E）", Locale.JAPANESE)), style = androidx.compose.material3.MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (uiState.scheduledCount > 0) {
                            Text("${uiState.completedCount} / ${uiState.scheduledCount} 件完了", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            todayEncouragement(uiState),
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    RoutinaMascot(
                        modifier = Modifier.size(72.dp),
                        contentDescription = "Routinaのマスコット",
                    )
                }
            }
            when {
                uiState.isLoading -> item { LoadingCard() }
                uiState.errorMessage != null && uiState.scheduledCount == 0 -> item { ErrorCard(uiState.errorMessage, onRetry) }
                uiState.isEmpty -> item { EmptyCard(onAddRoutine, onOpenRoutines) }
                else -> {
                    uiState.errorMessage?.let { message -> item { ErrorCard(message, onRetry) } }
                    if (uiState.isAllDone) item { AllDoneCard() }
                    if (pendingOrder.size > 1) item { Text("長押しで順番を変更", style = androidx.compose.material3.MaterialTheme.typography.bodySmall) }
                    items(pendingOrder, key = { it.routine.id }) { pendingItem ->
                        // Keep card sizes stable while picked up; update emphasis after dropping.
                        val displayOrder = dragStartingOrder ?: pendingOrder
                        val index = displayOrder.indexOfFirst { it.routine.id == pendingItem.routine.id }
                        ReorderablePending(
                            item = pendingItem,
                            dragState = dragState,
                            onStart = { dragStartingOrder = pendingOrder },
                            onMove = { direction ->
                                movePending(pendingItem, direction).also { moved ->
                                    if (moved) onReorderPending(pendingOrder.map { it.routine.id })
                                }
                            },
                            onEnd = ::finishPendingDrag,
                        ) { reorderModifier ->
                            Column(modifier = reorderModifier) {
                                if (index == 1) Text("残り", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                if (index == 0) NextRoutineCard(pendingItem, uiState.isProcessing, onComplete)
                                else PendingRoutineRow(pendingItem, uiState.isProcessing, onComplete)
                            }
                        }
                    }
                    if (uiState.completed.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .semantics {
                                        role = Role.Button
                                        stateDescription = if (completedExpanded) "展開中" else "折りたたみ"
                                    }
                                    .clickable { completedExpanded = !completedExpanded }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("完了済み ${uiState.completed.size}件", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(if (completedExpanded) "閉じる" else "開く", color = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                            }
                        }
                        if (completedExpanded) {
                            items(uiState.completed.size, key = { uiState.completed[it].routine.id }) { index ->
                                CompletedRoutineRow(uiState.completed[index], uiState.isProcessing, onCancel)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun todayEncouragement(uiState: TodayUiState): String = when {
    uiState.isLoading -> "今日の予定を確認しています"
    uiState.isAllDone -> "今日もおつかれさま！"
    uiState.isEmpty -> "自分のペースで始めましょう"
    else -> "小さな一歩を続けましょう"
}

@Composable private fun NextRoutineCard(item: TodayRoutine, isProcessing: Boolean, onComplete: (TodayRoutine) -> Unit, reorderModifier: Modifier = Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer), modifier = Modifier.fillMaxWidth().then(reorderModifier)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("次にやること", style = androidx.compose.material3.MaterialTheme.typography.labelLarge, color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer)
            Text(item.routine.name, style = androidx.compose.material3.MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Button(onClick = { onComplete(item) }, enabled = !isProcessing, modifier = Modifier.fillMaxWidth().semantics { contentDescription = "${item.routine.name}を完了にする" }) { Text("完了にする") }
        }
    }
}

@Composable private fun PendingRoutineRow(item: TodayRoutine, isProcessing: Boolean, onComplete: (TodayRoutine) -> Unit, reorderModifier: Modifier = Modifier) {
    Card(modifier = Modifier.fillMaxWidth().then(reorderModifier)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(item.routine.name, modifier = Modifier.weight(1f), style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            Spacer(Modifier.width(8.dp))
            TextButton(
                onClick = { onComplete(item) },
                enabled = !isProcessing,
                modifier = Modifier.semantics { contentDescription = "${item.routine.name}を完了にする" },
            ) { Text("完了") }
        }
    }
}

/** Whole tiles can be picked up after a long press; normal button taps remain untouched. */
@Composable
private fun ReorderablePending(
    item: TodayRoutine,
    dragState: RoutineDragState,
    onStart: () -> Unit,
    onMove: (Int) -> Boolean,
    onEnd: (Boolean) -> Unit,
    content: @Composable (Modifier) -> Unit,
) {
    val dragging = dragState.draggingId == item.routine.id
    val haptic = LocalHapticFeedback.current
    val currentOnStart by rememberUpdatedState(onStart)
    val currentOnMove by rememberUpdatedState(onMove)
    val currentOnEnd by rememberUpdatedState(onEnd)
    content(
        Modifier
            .semantics {
                contentDescription = "${item.routine.name}を長押しして並び替え"
                customActions = listOf(
                    CustomAccessibilityAction("上に移動") { currentOnMove(-1) },
                    CustomAccessibilityAction("下に移動") { currentOnMove(1) },
                )
            }
            .graphicsLayer { translationY = dragState.translation(item.routine.id) }
            .zIndex(if (dragging) 1f else 0f)
            .shadow(if (dragging) 8.dp else 0.dp)
            .pointerInput(item.routine.id) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { touch ->
                        dragState.start(item.routine.id, touch.y)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        currentOnStart()
                    },
                    onDragCancel = {
                        currentOnEnd(false)
                    },
                    onDragEnd = {
                        currentOnEnd(true)
                    },
                    onDrag = { change, amount ->
                        change.consume()
                        dragState.drag(amount.y)
                    },
                )
            },
    )
}

@Composable private fun CompletedRoutineRow(item: TodayRoutine, isProcessing: Boolean, onCancel: (TodayRoutine) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("✓ ${item.routine.name}", modifier = Modifier.weight(1f), style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            Spacer(Modifier.width(8.dp))
            TextButton(
                onClick = { onCancel(item) },
                enabled = !isProcessing,
                modifier = Modifier.semantics { contentDescription = "${item.routine.name}の完了を取り消す" },
            ) { Text("取消") }
        }
    }
}

@Composable
private fun LoadingCard() {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(message)
            Button(onClick = onRetry) { Text("再試行") }
        }
    }
}

@Composable
private fun EmptyCard(onAddRoutine: () -> Unit, onOpenRoutines: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("今日の予定はありません", style = androidx.compose.material3.MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("ルーティーンを追加して、毎日の行動を整えましょう。", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = onAddRoutine) { Text("ルーティーンを追加") }
            OutlinedButton(onClick = onOpenRoutines) { Text("ルーティーンを見る") }
        }
    }
}

@Composable
private fun AllDoneCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("今日の予定はすべて完了です", style = androidx.compose.material3.MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("おつかれさまでした。", color = androidx.compose.material3.MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}

internal fun millisUntilNextTodayBoundary(now: ZonedDateTime): Long =
    Duration.between(now, now.toLocalDate().plusDays(1).atStartOfDay(now.zone)).toMillis().coerceAtLeast(1)
