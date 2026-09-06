@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.routina.app.feature.reflection

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Tab
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.routina.app.domain.repository.RoutineRepository
import com.routina.app.feature.calendar.CalendarRoute
import com.routina.app.feature.character.CharacterRoute

private enum class ReflectionTab(val label: String) {
    HISTORY("履歴"),
    GROWTH("成長"),
}

/** Combines past completion records and character progress under one reflection destination. */
@Composable
fun ReflectionRoute(
    repository: RoutineRepository,
    onAddRoutine: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTabName by rememberSaveable { mutableStateOf(ReflectionTab.HISTORY.name) }
    val selectedTab = ReflectionTab.entries.firstOrNull { it.name == selectedTabName }
        ?: ReflectionTab.HISTORY

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(title = { Text("ふりかえり") })
        SecondaryTabRow(selectedTabIndex = selectedTab.ordinal) {
            ReflectionTab.entries.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTabName = tab.name },
                    text = { Text(tab.label) },
                    modifier = Modifier.semantics {
                        contentDescription = "${tab.label}タブ"
                    },
                )
            }
        }

        when (selectedTab) {
            ReflectionTab.HISTORY -> CalendarRoute(
                repository = repository,
                onAddRoutine = onAddRoutine,
                modifier = Modifier.weight(1f),
                showTopAppBar = false,
            )

            ReflectionTab.GROWTH -> CharacterRoute(
                repository = repository,
                modifier = Modifier.weight(1f),
                showTitle = false,
            )
        }
    }
}
