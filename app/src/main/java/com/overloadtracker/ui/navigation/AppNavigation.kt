/**
 * Root navigation graph with floating Liquid Glass bottom bar and nested workout flows.
 */
package com.overloadtracker.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.overloadtracker.R
import com.overloadtracker.ui.components.AtmosphericBackground
import com.overloadtracker.ui.components.LiquidGlassBottomBar
import com.overloadtracker.ui.components.NavItem
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
 * Host composable wiring all app destinations and the floating Liquid Glass bottom navigation bar.
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

    AtmosphericBackground(modifier = modifier) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                if (showBottomBar) {
                    val navItems = listOf(
                        NavItem(
                            route = GroupsRoute,
                            title = stringResource(R.string.nav_groups),
                            icon = Icons.Default.List,
                            isSelected = destination?.hasRoute(GroupsRoute::class) == true,
                            onClick = {
                                navController.navigate(GroupsRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        ),
                        NavItem(
                            route = LibraryRoute,
                            title = stringResource(R.string.nav_library),
                            icon = Icons.Default.FitnessCenter,
                            isSelected = destination?.hasRoute(LibraryRoute::class) == true,
                            onClick = {
                                navController.navigate(LibraryRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        ),
                        NavItem(
                            route = HistoryRoute,
                            title = stringResource(R.string.nav_history),
                            icon = Icons.Default.History,
                            isSelected = destination?.hasRoute(HistoryRoute::class) == true,
                            onClick = {
                                navController.navigate(HistoryRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        ),
                        NavItem(
                            route = SettingsRoute,
                            title = stringResource(R.string.nav_settings),
                            icon = Icons.Default.Settings,
                            isSelected = destination?.hasRoute(SettingsRoute::class) == true,
                            onClick = {
                                navController.navigate(SettingsRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    )
                    LiquidGlassBottomBar(items = navItems)
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(padding),
                enterTransition = { fadeIn(animationSpec = tween(200)) + slideInHorizontally(animationSpec = tween(200)) { fullWidth -> fullWidth / 4 } },
                exitTransition = { fadeOut(animationSpec = tween(150)) },
                popEnterTransition = { fadeIn(animationSpec = tween(150)) },
                popExitTransition = { fadeOut(animationSpec = tween(200)) + slideOutHorizontally(animationSpec = tween(200)) { fullWidth -> fullWidth / 4 } }
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
}
