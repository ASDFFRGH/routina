package com.routina.app.ui.components

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.isActive

/** Tracks the lifted item in viewport coordinates, independently of changing row heights. */
internal class RoutineDragState(
    private val listState: LazyListState,
    private val keys: () -> List<String>,
    private val move: (String, String) -> Boolean,
) {
    var draggingId by mutableStateOf<String?>(null)
        private set
    private var top by mutableStateOf(0f)
    private var pointerY by mutableStateOf(0f)
    private var initialSize = 0
    private var awaitingIndex: Int? = null

    fun start(id: String, touchY: Float) {
        val item = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == id } ?: return
        top = item.offset.toFloat()
        pointerY = top + touchY
        initialSize = item.size
        awaitingIndex = null
        draggingId = id
    }

    fun drag(delta: Float) {
        top += delta
        pointerY += delta
        moveAcrossNeighbor()
    }

    fun translation(id: String): Float {
        if (draggingId != id) return 0f
        val item = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == id } ?: return 0f
        return top - item.offset
    }

    fun reset() {
        draggingId = null
        awaitingIndex = null
    }

    suspend fun update(edge: Float, speed: Float) {
        if (draggingId == null) return
        val layout = listState.layoutInfo
        val scroll = when {
            pointerY < layout.viewportStartOffset + edge ->
                -speed * ((layout.viewportStartOffset + edge - pointerY) / edge).coerceIn(0f, 1f)
            pointerY > layout.viewportEndOffset - edge ->
                speed * ((pointerY - layout.viewportEndOffset + edge) / edge).coerceIn(0f, 1f)
            else -> 0f
        }
        if (scroll != 0f) listState.scrollBy(scroll)
        moveAcrossNeighbor()
    }

    private fun moveAcrossNeighbor() {
        val id = draggingId ?: return
        val layout = listState.layoutInfo
        val keys = keys()
        val item = layout.visibleItemsInfo.firstOrNull { it.key == id } ?: return
        // Wait for a moved item to be laid out before considering the next crossing.
        awaitingIndex?.let { expected ->
            if (item.index != expected) return
            awaitingIndex = null
        }
        val from = keys.indexOf(id)
        if (from < 0) return
        val center = top + initialSize / 2f
        val direction = if (center > item.offset + item.size / 2f) 1 else -1
        val targetId = keys.getOrNull(from + direction) ?: return
        val target = layout.visibleItemsInfo.firstOrNull { it.key == targetId } ?: return
        val targetCenter = target.offset + target.size / 2f
        if ((direction > 0 && center > targetCenter) || (direction < 0 && center < targetCenter)) {
            if (move(id, targetId)) awaitingIndex = target.index
        }
    }
}

@Composable
internal fun rememberRoutineDragState(
    listState: LazyListState,
    keys: List<String>,
    onMove: (String, String) -> Boolean,
): RoutineDragState {
    val currentKeys by rememberUpdatedState(keys)
    val currentOnMove by rememberUpdatedState(onMove)
    val state = remember(listState) { RoutineDragState(listState, { currentKeys }, { id, target -> currentOnMove(id, target) }) }
    val edge = with(LocalDensity.current) { 72.dp.toPx() }
    val speed = with(LocalDensity.current) { 18.dp.toPx() }
    LaunchedEffect(state.draggingId) {
        while (isActive && state.draggingId != null) {
            withFrameNanos { }
            state.update(edge, speed)
        }
    }
    return state
}
