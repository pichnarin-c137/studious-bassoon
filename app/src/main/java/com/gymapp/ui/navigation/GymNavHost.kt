package com.gymapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.gymapp.ui.screens.activity.ActivityScreen
import com.gymapp.ui.screens.checkin.CheckInScreen
import com.gymapp.ui.screens.home.HomeScreen
import com.gymapp.ui.screens.log.LogWorkoutScreen
import com.gymapp.ui.screens.membership.MembershipScreen
import com.gymapp.ui.screens.profile.ProfileScreen
import com.gymapp.ui.screens.progress.ProgressScreen
import com.gymapp.ui.screens.session.SessionScreen

@Composable
fun GymNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Destination.START.route,
        modifier = modifier,
    ) {
        composable(Destination.HOME.route) {
            HomeScreen(
                onOpenMembership = { navController.navigate(Routes.MEMBERSHIP) },
                onShowQr = { navController.navigate(Routes.CHECKIN) },
                onStartWorkout = { navController.navigateToLog() },
            )
        }
        composable(Destination.PROGRESS.route) { ProgressScreen() }
        composable(Destination.ACTIVITY.route) { ActivityScreen() }
        composable(Destination.PROFILE.route) { ProfileScreen() }

        // Center action + detail screens (bottom bar hidden, back arrow shown).
        composable(Routes.LOG) {
            LogWorkoutScreen(onStartSession = { navController.navigate(Routes.SESSION) })
        }
        composable(Routes.SESSION) { SessionScreen(onExit = { navController.popBackStack() }) }
        composable(Routes.MEMBERSHIP) {
            MembershipScreen(onShowQr = { navController.navigate(Routes.CHECKIN) })
        }
        composable(Routes.CHECKIN) { CheckInScreen() }
    }
}

/** The center "+" can be tapped repeatedly; keep a single Log entry on the back stack. */
fun NavHostController.navigateToLog() {
    navigate(Routes.LOG) { launchSingleTop = true }
}
