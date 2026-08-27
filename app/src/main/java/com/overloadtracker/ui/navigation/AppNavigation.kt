/**
 * Root navigation graph with bottom bar and nested workout flows.
 * Refactored with Liquid Glass / Liquid Vitality visual aesthetic and smooth transitions.
 */
package com.overloadtracker.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Scaffold
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
import com.overloadtracker.ui.components.LiquidBottomNavBar
import com.overloadtracker.ui.components.LiquidNavItem
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
import com.overloadtracker.ui.theme.MidnightBackground

/**
 * Host composable wiring all app destinations and the main Liquid Glass bottom navigation bar.
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

    val currentRoute = when {
        destination?.hasRoute(GroupsRoute::class) == true -> "groups"
        destination?.hasRoute(LibraryRoute::class) == true -> "library"
        destination?.hasRoute(HistoryRoute::class) == true -> "history"
        destination?.hasRoute(SettingsRoute::class) == true -> "settings"
        else -> null
    }

    val navItems = listOf(
        LiquidNavItem(
            route = "groups",
            title = stringResource(R.string.nav_groups),
            icon = Icons.AutoMirrored.Filled.List
        ),
        LiquidNavItem(
            route = "library",
            title = stringResource(R.string.nav_library),
            icon = Icons.Default.FitnessCenter
        ),
        LiquidNavItem(
            route = "history",
            title = stringResource(R.string.nav_history),
            icon = Icons.Default.History
        ),
        LiquidNavItem(
            route = "settings",
            title = stringResource(R.string.nav_settings),
            icon = Icons.Default.Settings
        )
    )

    val startDestination = if (hasOnboarded) GroupsRoute else OnboardingRoute

    Scaffold(
        modifier = modifier.background(MidnightBackground),
        containerColor = MidnightBackground,
        bottomBar = {
            if (showBottomBar) {
                LiquidBottomNavBar(
                    items = navItems,
                    currentRoute = currentRoute,
                    onItemSelected = { item ->
                        val targetRoute = when (item.route) {
                            "groups" -> GroupsRoute
                            "library" -> LibraryRoute
                            "history" -> HistoryRoute
                            "settings" -> SettingsRoute
                            else -> GroupsRoute
                        }
                        navController.navigate(targetRoute) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding),
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) }
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
            composable<LiveWorkoutRoute>(
                enterTransition = { slideInVertically(initialOffsetY = { it }, animationSpec = tween(350)) + fadeIn() },
                exitTransition = { slideOutVertically(targetOffsetY = { it }, animationSpec = tween(350)) + fadeOut() },
                popExitTransition = { slideOutVertically(targetOffsetY = { it }, animationSpec = tween(350)) + fadeOut() }
            ) {
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
