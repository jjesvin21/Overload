/**
 * Root navigation graph with bottom bar and nested workout flows.
 */
package com.overloadtracker.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.overloadtracker.R
import com.overloadtracker.ui.screens.groups.AddExercisesToGroupScreen
import com.overloadtracker.ui.screens.groups.GroupEditorScreen
import com.overloadtracker.ui.screens.groups.MyGroupsScreen
import com.overloadtracker.ui.screens.history.ExerciseProgressScreen
import com.overloadtracker.ui.screens.history.HistoryScreen
import com.overloadtracker.ui.screens.history.SessionDetailScreen
import com.overloadtracker.ui.screens.library.ExerciseLibraryScreen
import com.overloadtracker.ui.screens.onboarding.OnboardingScreen
import com.overloadtracker.ui.screens.settings.SettingsScreen
import com.overloadtracker.ui.screens.workout.LiveWorkoutScreen

/**
 * Host composable wiring all app destinations and the main bottom navigation bar.
 */
@Composable
fun AppNavigation(
    hasOnboarded: Boolean,
    onMarkOnboarded: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination

    val showBottomBar = destination?.hasRoute(GroupsRoute::class) == true ||
        destination?.hasRoute(LibraryRoute::class) == true ||
        destination?.hasRoute(HistoryRoute::class) == true ||
        destination?.hasRoute(SettingsRoute::class) == true

    val startDestination = if (hasOnboarded) GroupsRoute else OnboardingRoute

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = destination?.hasRoute(GroupsRoute::class) == true,
                        onClick = {
                            navController.navigate(GroupsRoute) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.List, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_groups)) }
                    )
                    NavigationBarItem(
                        selected = destination?.hasRoute(LibraryRoute::class) == true,
                        onClick = {
                            navController.navigate(LibraryRoute) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.FitnessCenter, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_library)) }
                    )
                    NavigationBarItem(
                        selected = destination?.hasRoute(HistoryRoute::class) == true,
                        onClick = {
                            navController.navigate(HistoryRoute) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.History, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_history)) }
                    )
                    NavigationBarItem(
                        selected = destination?.hasRoute(SettingsRoute::class) == true,
                        onClick = {
                            navController.navigate(SettingsRoute) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text(stringResource(R.string.nav_settings)) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        ) {
            composable<OnboardingRoute> {
                OnboardingScreen(onComplete = {
                    onMarkOnboarded()
                    navController.navigate(GroupsRoute) {
                        popUpTo(OnboardingRoute) { inclusive = true }
                    }
                })
            }
            composable<GroupsRoute> {
                MyGroupsScreen(
                    onStartWorkout = { groupId ->
                        navController.navigate(LiveWorkoutRoute(groupId))
                    },
                    onEditGroup = { groupId ->
                        navController.navigate(GroupEditorRoute(groupId))
                    }
                )
            }
            composable<LibraryRoute> {
                ExerciseLibraryScreen(
                    onViewProgress = { exerciseId ->
                        navController.navigate(ExerciseProgressRoute(exerciseId))
                    }
                )
            }
            composable<HistoryRoute> {
                HistoryScreen(
                    onSessionClick = { sessionId ->
                        navController.navigate(SessionDetailRoute(sessionId))
                    }
                )
            }
            composable<SettingsRoute> {
                SettingsScreen()
            }
            composable<GroupEditorRoute> {
                GroupEditorScreen(
                    onBack = { navController.popBackStack() },
                    onAddExercises = { groupId ->
                        navController.navigate(AddExercisesRoute(groupId))
                    }
                )
            }
            composable<AddExercisesRoute> {
                AddExercisesToGroupScreen(
                    onBack = { navController.popBackStack() },
                    onAdded = { navController.popBackStack() }
                )
            }
            composable<LiveWorkoutRoute> {
                LiveWorkoutScreen(
                    onFinish = {
                        navController.popBackStack<LiveWorkoutRoute>(inclusive = true)
                    },
                    onDiscard = { navController.popBackStack() },
                    onViewProgress = { exerciseId ->
                        navController.navigate(ExerciseProgressRoute(exerciseId))
                    }
                )
            }
            composable<SessionDetailRoute> {
                SessionDetailScreen(onBack = { navController.popBackStack() })
            }
            composable<ExerciseProgressRoute> {
                ExerciseProgressScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
