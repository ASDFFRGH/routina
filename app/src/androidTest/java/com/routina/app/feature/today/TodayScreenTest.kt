package com.routina.app.feature.today

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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

    private fun setScreen(
        state: TodayUiState,
        onComplete: (TodayRoutine) -> Unit = {},
        onCancel: (TodayRoutine) -> Unit = {},
        onRetry: () -> Unit = {},
        onAdd: () -> Unit = {},
        onOpen: () -> Unit = {},
    ) {
        composeRule.setContent {
            RoutinaTheme {
                TodayScreen(state, onComplete, onCancel, onRetry, onAdd, onOpen)
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
}
