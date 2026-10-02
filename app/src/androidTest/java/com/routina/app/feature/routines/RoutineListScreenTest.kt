package com.routina.app.feature.routines

import androidx.activity.ComponentActivity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.TouchInjectionScope
import androidx.compose.ui.test.junit4.v2.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.routina.app.domain.model.Frequency
import com.routina.app.domain.model.Routine
import com.routina.app.ui.theme.RoutinaTheme
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class RoutineListScreenTest {
    private val activityRule = ActivityScenarioRule(ComponentActivity::class.java)

    @get:Rule
    val composeRule = AndroidComposeTestRule(activityRule) { rule ->
        var activity: ComponentActivity? = null
        rule.scenario.onActivity { activity = it }
        requireNotNull(activity)
    }

    @Test
    fun long_pressing_anywhere_on_row_reorders_routines() {
        val first = item("最初", 0)
        val second = item("次", 1)
        var reordered: List<String>? = null
        setScreen(listOf(first, second), onReorder = { reordered = it.map { item -> item.routine.id } })

        composeRule.onNodeWithContentDescription("最初を長押しして並び替え")
            .performTouchInput { longPressAndDragBy(Offset(0f, 500f)) }

        composeRule.runOnIdle { assertEquals(listOf("次", "最初"), reordered) }
    }

    @Test
    fun archive_button_remains_clickable() {
        val routine = item("読書", 0)
        var archived: Routine? = null
        setScreen(listOf(routine), onArchive = { archived = it })

        composeRule.onNodeWithText("アーカイブ").performClick()

        composeRule.runOnIdle { assertEquals(routine.routine, archived) }
    }

    private fun setScreen(
        routines: List<RoutineListItem>,
        onArchive: (Routine) -> Unit = {},
        onReorder: (List<RoutineListItem>) -> Unit = {},
    ) {
        composeRule.setContent {
            RoutinaTheme { RoutineListScreen(routines, {}, onArchive, onReorder) }
        }
    }

    private fun item(id: String, order: Long) = RoutineListItem(
        Routine(id, id, LocalDate.of(2026, 9, 6), Frequency.Daily, 1, 1, order, sortOrder = order),
        streak = 0,
    )

    private fun TouchInjectionScope.longPressAndDragBy(offset: Offset) {
        down(center)
        advanceEventTime(700)
        moveBy(offset)
        up()
    }
}
