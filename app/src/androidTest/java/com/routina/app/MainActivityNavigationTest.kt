package com.routina.app

import android.content.ComponentName
import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.junit4.v2.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test

class MainActivityNavigationTest {
    private val activityRule = ActivityScenarioRule<MainActivity>(
        Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            component = ComponentName(
                InstrumentationRegistry.getInstrumentation().targetContext.packageName,
                MainActivity::class.java.name,
            )
        },
    )

    @get:Rule
    val composeRule = AndroidComposeTestRule(activityRule) { rule ->
        var activity: MainActivity? = null
        rule.scenario.onActivity { activity = it }
        requireNotNull(activity)
    }

    @Test
    fun launchesOnTodayAndNavigatesBetweenTopLevelDestinations() {
        composeRule.onNodeWithContentDescription("今日タブ").assertIsSelected()

        composeRule.onNodeWithContentDescription("ルーティーンタブ").performClick()
        composeRule.onNodeWithContentDescription("ルーティーンタブ").assertIsSelected()

        composeRule.onNodeWithContentDescription("ふりかえりタブ").performClick()
        composeRule.onNodeWithContentDescription("ふりかえりタブ").assertIsSelected()
        composeRule.onNodeWithContentDescription("履歴タブ").assertIsSelected()

        composeRule.onNodeWithContentDescription("成長タブ").performClick()
        composeRule.onNodeWithContentDescription("成長タブ").assertIsSelected()
        composeRule.onNodeWithText("総ポイント").assertIsDisplayed()

        composeRule.onNodeWithContentDescription("今日タブ").performClick()
        composeRule.onNodeWithContentDescription("ふりかえりタブ").performClick()
        composeRule.onNodeWithContentDescription("成長タブ").assertIsSelected()
    }

    @Test
    fun routineFormCancelsBackToEachEntryPoint() {
        composeRule.onNodeWithContentDescription("今日タブ").performClick()
        composeRule.onNodeWithContentDescription("ルーティーンを追加").performClick()
        composeRule.onNodeWithText("ルーティーンを登録").assertIsDisplayed()
        assertBottomNavigationIsHidden()
        composeRule.onNodeWithText("キャンセル").performClick()
        composeRule.onNodeWithContentDescription("今日タブ").assertIsSelected()

        composeRule.onNodeWithContentDescription("ルーティーンタブ").performClick()
        composeRule.onAllNodesWithText("追加")[0].performClick()
        composeRule.onNodeWithText("ルーティーンを登録").assertIsDisplayed()
        assertBottomNavigationIsHidden()
        composeRule.onNodeWithText("キャンセル").performClick()
        composeRule.onNodeWithContentDescription("ルーティーンタブ").assertIsSelected()

        composeRule.onNodeWithContentDescription("ふりかえりタブ").performClick()
        composeRule.onNodeWithContentDescription("履歴タブ").assertIsSelected()
        composeRule.onAllNodesWithText("追加")[0].performClick()
        composeRule.onNodeWithText("ルーティーンを登録").assertIsDisplayed()
        assertBottomNavigationIsHidden()
        composeRule.onNodeWithText("キャンセル").performClick()
        composeRule.onNodeWithContentDescription("ふりかえりタブ").assertIsSelected()
        composeRule.onNodeWithContentDescription("履歴タブ").assertIsSelected()
    }

    private fun assertBottomNavigationIsHidden() {
        composeRule.onNodeWithContentDescription("今日タブ").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("ルーティーンタブ").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("ふりかえりタブ").assertDoesNotExist()
    }
}
