package com.routina.app.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.routina.app.domain.repository.RoutineRepository
import com.routina.app.feature.calendar.CalendarRoute
import com.routina.app.feature.character.CharacterRoute
import com.routina.app.feature.routines.RoutineFormRoute
import com.routina.app.feature.routines.RoutineListRoute
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppDestination {
    @Serializable data object Calendar : AppDestination
    @Serializable data object Routines : AppDestination
    @Serializable data object Character : AppDestination
    @Serializable data object RoutineForm : AppDestination
}

private data class TopLevelDestination(
    val route: AppDestination,
    val symbol: String,
    val label: String,
)

private val topLevelDestinations = listOf(
    TopLevelDestination(AppDestination.Calendar, "▦", "カレンダー"),
    TopLevelDestination(AppDestination.Routines, "✓", "ルーティーン"),
    TopLevelDestination(AppDestination.Character, "✦", "成長"),
)

@Composable
fun RoutinaApp(repository: RoutineRepository) {
    val navController = rememberNavController()
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    val isTopLevelDestination = topLevelDestinations.any { destination ->
        currentDestination?.hasRoute(destination.route::class) == true
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (isTopLevelDestination) {
                RoutinaNavigationBar(
                    currentDestination = currentDestination,
                    onDestinationSelected = { destination ->
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.Calendar,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable<AppDestination.Calendar> {
                CalendarRoute(repository, onAddRoutine = { navController.navigate(AppDestination.RoutineForm) })
            }
            composable<AppDestination.Routines> {
                RoutineListRoute(repository, onAddRoutine = { navController.navigate(AppDestination.RoutineForm) })
            }
            composable<AppDestination.Character> { CharacterRoute(repository) }
            composable<AppDestination.RoutineForm> {
                RoutineFormRoute(
                    repository = repository,
                    onSaved = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() },
                )
            }
        }
    }
}

@Composable
private fun RoutinaNavigationBar(
    currentDestination: NavDestination?,
    onDestinationSelected: (TopLevelDestination) -> Unit,
) {
    NavigationBar {
        topLevelDestinations.forEach { destination ->
            NavigationBarItem(
                selected = currentDestination?.hasRoute(destination.route::class) == true,
                onClick = { onDestinationSelected(destination) },
                icon = {
                    Text(
                        text = destination.symbol,
                        modifier = Modifier.semantics { contentDescription = "${destination.label}タブ" },
                    )
                },
                label = { Text(destination.label) },
            )
        }
    }
}
