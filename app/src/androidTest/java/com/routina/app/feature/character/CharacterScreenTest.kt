package com.routina.app.feature.character

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.routina.app.domain.model.CharacterStage
import com.routina.app.ui.theme.RoutinaTheme
import org.junit.Rule
import org.junit.Test

class CharacterScreenTest {
    private val activityRule = ActivityScenarioRule(ComponentActivity::class.java)

    @get:Rule
    val composeRule = AndroidComposeTestRule(activityRule) { rule ->
        var activity: ComponentActivity? = null
        rule.scenario.onActivity { activity = it }
        requireNotNull(activity)
    }

    @Test
    fun shows_active_routine_names_with_zero_and_nonzero_scheduled_day_streaks() {
        setScreen(listOf(CharacterRoutineStreak("one", "読書", 4), CharacterRoutineStreak("two", "運動", 0)))

        composeRule.onNodeWithText("継続中").assertIsDisplayed()
        composeRule.onNodeWithText("連続継続日数（予定日）").assertIsDisplayed()
        composeRule.onNodeWithText("読書").assertIsDisplayed()
        composeRule.onNodeWithText("4日").assertIsDisplayed()
        composeRule.onNodeWithText("運動").assertIsDisplayed()
        composeRule.onNodeWithText("0日").assertIsDisplayed()
    }

    @Test
    fun shows_empty_growth_state() {
        setScreen(emptyList())
        composeRule.onNodeWithText("継続中のルーティーンはありません").assertIsDisplayed()
    }

    private fun setScreen(streaks: List<CharacterRoutineStreak>) {
        composeRule.setContent {
            RoutinaTheme {
                CharacterScreen(
                    CharacterUiState(1, 0, 0, 0, 100, 0f, CharacterStage.NOVICE, streaks),
                )
            }
        }
    }
}
