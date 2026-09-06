package com.routina.app.feature.calendar

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.junit4.v2.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.routina.app.domain.model.Frequency
import com.routina.app.domain.model.Routine
import com.routina.app.ui.theme.RoutinaTheme
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test

class CalendarScreenTest {
    private val activityRule = ActivityScenarioRule(ComponentActivity::class.java)

    @get:Rule
    val composeRule = AndroidComposeTestRule(activityRule) { rule ->
        var activity: ComponentActivity? = null
        rule.scenario.onActivity { activity = it }
        requireNotNull(activity)
    }

    @Test
    fun day_cell_expands_for_large_font_scale() {
        val date = LocalDate.of(2026, 9, 6)
        composeRule.setContent {
            val deviceDensity = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density = deviceDensity.density, fontScale = 2f),
            ) {
                RoutinaTheme {
                    MonthGrid(
                        days = listOf(
                            CalendarDay(
                                date = date,
                                isCurrentMonth = true,
                                scheduledCount = 1,
                                completedCount = 0,
                                status = DayStatus.PENDING,
                            ),
                        ),
                        selectedDate = date,
                        today = date,
                        onSelectDate = {},
                    )
                }
            }
        }

        composeRule.onNodeWithContentDescription(
            "2026-09-06、今日、選択中、未実行、0/1件完了",
        ).assertHeightIsAtLeast(56.dp)
    }

    @Test
    fun day_routine_checkbox_names_the_routine_for_accessibility() {
        val date = LocalDate.of(2026, 9, 6)
        val routine = Routine(
            id = "morning-walk",
            name = "朝の散歩",
            startDate = date,
            frequency = Frequency.EveryDays(1),
            rewardXp = 20,
            rewardPoints = 10,
            createdAtEpochMillis = 1,
        )
        composeRule.setContent {
            RoutinaTheme {
                DayRoutineList(
                    routines = listOf(ScheduledRoutine(routine, isCompleted = false)),
                    canEdit = true,
                    onToggleCompletion = {},
                    onAddRoutine = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("朝の散歩を完了にする").assertIsDisplayed()
    }
}
