package com.routina.app.feature.today

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import org.junit.Assert.assertTrue
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.geometry.Offset
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.routina.app.domain.model.Frequency
import com.routina.app.domain.model.Routine
import com.routina.app.ui.theme.RoutinaTheme
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TodayScreenTest {
    private val date = LocalDate.of(2026, 9, 6)
    private val activityRule = ActivityScenarioRule(ComponentActivity::class.java)

    @get:Rule
    val composeRule = AndroidComposeTestRule(activityRule) { rule ->
        var activity: ComponentActivity? = null
        rule.scenario.onActivity { activity = it }
        requireNotNull(activity)
    }

    @Test
    fun empty_state_offers_add_and_routine_management() {
        var additions = 0
        var opens = 0
        setScreen(TodayUiState(date = date), onAdd = { additions++ }, onOpen = { opens++ })

        composeRule.onNodeWithText("今日の予定はありません").assertIsDisplayed()
        composeRule.onNodeWithText("ルーティーンを見る").performClick()
        composeRule.onNodeWithContentDescription("ルーティーンを追加").performClick()

        assertEquals(1, additions)
        assertEquals(1, opens)
    }

    @Test
    fun partial_state_delegates_complete_and_cancel() {
        val pending = item("朝の散歩", completed = false)
        val completed = item("水を飲む", completed = true)
        var completedItem: TodayRoutine? = null
        var cancelledItem: TodayRoutine? = null
        setScreen(
            TodayUiState(date = date, pending = listOf(pending), completed = listOf(completed)),
            onComplete = { completedItem = it },
            onCancel = { cancelledItem = it },
        )

        composeRule.onNodeWithContentDescription("朝の散歩を完了にする").performClick()
        composeRule.onNodeWithText("開く").performClick()
        composeRule.onNodeWithText("取消").performClick()

        assertEquals(pending, completedItem)
        assertEquals(completed, cancelledItem)
    }

    @Test
    fun all_done_state_is_visible() {
        setScreen(TodayUiState(date = date, completed = listOf(item("読書", completed = true))))
        composeRule.onNodeWithText("今日の予定はすべて完了です").assertIsDisplayed()
    }

    @Test
    fun error_state_delegates_retry() {
        var retries = 0
        setScreen(TodayUiState.error(date, "読み込めませんでした"), onRetry = { retries++ })
        composeRule.onNodeWithText("読み込めませんでした").assertIsDisplayed()
        composeRule.onNodeWithText("再試行").performClick()
        assertEquals(1, retries)
    }

    @Test
    fun long_press_moves_next_tile_below_variable_height_remaining_tile() {
        val first = item("長い名前の最初のルーティン", completed = false)
        val second = item("次のルーティン", completed = false)
        var reordered: List<String>? = null
        setScreen(
            TodayUiState(date = date, pending = listOf(first, second)),
            onReorder = { reordered = it },
        )

        composeRule.onNodeWithContentDescription("長い名前の最初のルーティンを長押しして並び替え")
            .performTouchInput { longPressAndDragBy(Offset(0f, 500f)) }

        composeRule.runOnIdle { assertEquals(listOf("次のルーティン", "長い名前の最初のルーティン"), reordered) }
    }

    @Test
    fun long_press_moves_remaining_tile_into_first_position() {
        val first = item("最初", completed = false)
        val second = item("次", completed = false)
        var reordered: List<String>? = null
        setScreen(TodayUiState(date = date, pending = listOf(first, second)), onReorder = { reordered = it })

        composeRule.onNodeWithContentDescription("次を長押しして並び替え")
            .performTouchInput { longPressAndDragBy(Offset(0f, -500f)) }

        composeRule.runOnIdle { assertEquals(listOf("次", "最初"), reordered) }
    }

    @Test
    fun cancelled_drag_keeps_original_order_and_does_not_persist() {
        val first = item("最初", completed = false)
        val second = item("次", completed = false)
        var reordered: List<String>? = null
        setScreen(TodayUiState(date = date, pending = listOf(first, second)), onReorder = { reordered = it })

        composeRule.onNodeWithContentDescription("最初を長押しして並び替え")
            .performTouchInput {
                down(center)
                advanceEventTime(700)
                moveBy(Offset(0f, 500f))
                cancel()
            }

        composeRule.runOnIdle { assertEquals(null, reordered) }
        composeRule.onNodeWithText("最初").assertIsDisplayed()
    }

    @Test
    fun holding_tile_near_bottom_scrolls_to_routines_beyond_initial_viewport() {
        val pending = (1..12).map { item("習慣$it", completed = false) }
        var reordered: List<String>? = null
        setScreen(TodayUiState(date = date, pending = pending), onReorder = { reordered = it })
        val tile = composeRule.onNodeWithContentDescription("習慣1を長押しして並び替え")
        val tileBounds = tile.fetchSemanticsNode().boundsInRoot
        val rootBounds = composeRule.onRoot().fetchSemanticsNode().boundsInRoot
        composeRule.mainClock.autoAdvance = false
        tile.performTouchInput {
            down(center)
            advanceEventTime(700)
            moveTo(Offset(center.x, rootBounds.bottom - tileBounds.top - 160f))
        }
        // The pointer stays still while frames drive edge scrolling and neighbor crossings.
        composeRule.mainClock.advanceTimeBy(2_000)
        composeRule.onRoot().performTouchInput { up() }
        composeRule.mainClock.autoAdvance = true
        composeRule.runOnIdle {
            assertTrue("The held tile should reach beyond the initially visible tiles", (reordered?.indexOf("習慣1") ?: -1) > 5)
        }
    }

    private fun setScreen(
        state: TodayUiState,
        onComplete: (TodayRoutine) -> Unit = {},
        onCancel: (TodayRoutine) -> Unit = {},
        onRetry: () -> Unit = {},
        onAdd: () -> Unit = {},
        onOpen: () -> Unit = {},
        onReorder: (List<String>) -> Unit = {},
    ) {
        composeRule.setContent {
            RoutinaTheme {
                TodayScreen(state, onComplete, onCancel, onRetry, onAdd, onOpen, onReorderPending = onReorder)
            }
        }
    }

    private fun item(name: String, completed: Boolean): TodayRoutine {
        val routine = Routine(
            id = name,
            name = name,
            startDate = date,
            frequency = Frequency.EveryDays(1),
            rewardXp = 1,
            rewardPoints = 1,
            createdAtEpochMillis = 1,
        )
        return TodayRoutine(routine, date, completed)
    }

    private fun androidx.compose.ui.test.TouchInjectionScope.longPressAndDragBy(offset: Offset) {
        down(center)
        advanceEventTime(700)
        moveBy(offset)
        up()
    }
}
