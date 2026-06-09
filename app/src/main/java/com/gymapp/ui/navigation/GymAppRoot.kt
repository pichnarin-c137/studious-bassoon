package com.gymapp.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gymapp.R
import com.gymapp.ui.theme.Spacing
import com.gymapp.util.LocalWindowSize
import com.gymapp.util.WindowSize

/** Inactive tab tint (spec): gray icons, lime only on the active tab. */
private val InactiveTint = Color(0xFF5A5A60)

/**
 * Responsive app shell. Compact/Medium use a custom bottom bar with a raised lime center "+" for
 * logging a workout; Expanded (tablets) uses a side rail. Detail screens (Log, Membership,
 * Check-in) hide the bar and show a back arrow instead.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymAppRoot() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route
    val current = Destination.fromRoute(route)
    val isDetail = route in Routes.detailRoutes
    val useRail = LocalWindowSize.current == WindowSize.EXPANDED

    Scaffold(
        topBar = {
            if (isDetail) {
                TopAppBar(
                    title = { Text(stringResource(detailTitleRes(route))) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                    },
                )
            }
        },
        bottomBar = {
            if (!useRail && !isDetail) {
                GymBottomBar(
                    current = current,
                    onSelect = { navController.navigateTopLevel(it) },
                    onLog = { navController.navigateToLog() },
                )
            }
        },
    ) { padding ->
        Row(Modifier.fillMaxSize().padding(padding)) {
            if (useRail) {
                GymRail(
                    current = current,
                    onSelect = { navController.navigateTopLevel(it) },
                    onLog = { navController.navigateToLog() },
                )
            }
            GymNavHost(navController, Modifier.fillMaxSize())
        }
    }
}

@StringRes
private fun detailTitleRes(route: String?): Int = when (route) {
    Routes.MEMBERSHIP -> R.string.nav_membership
    Routes.CHECKIN -> R.string.checkin_title
    Routes.LOG -> R.string.nav_log
    else -> R.string.app_name
}

@Composable
private fun GymBottomBar(
    current: Destination,
    onSelect: (Destination) -> Unit,
    onLog: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column {
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(horizontal = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BarItem(Destination.HOME, current, onSelect, Modifier.weight(1f))
                BarItem(Destination.PROGRESS, current, onSelect, Modifier.weight(1f))
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { CenterFab(onLog) }
                BarItem(Destination.ACTIVITY, current, onSelect, Modifier.weight(1f))
                BarItem(Destination.PROFILE, current, onSelect, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun BarItem(
    dest: Destination,
    current: Destination,
    onSelect: (Destination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = if (current == dest) MaterialTheme.colorScheme.primary else InactiveTint
    Column(
        modifier = modifier.clickable { onSelect(dest) },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Icon(
            painter = painterResource(dest.iconRes),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = stringResource(dest.labelRes),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            maxLines = 1,
        )
    }
}

@Composable
private fun CenterFab(onLog: () -> Unit) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable { onLog() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_nav_log_add),
            contentDescription = stringResource(R.string.nav_log_cd),
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(28.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GymRail(
    current: Destination,
    onSelect: (Destination) -> Unit,
    onLog: () -> Unit,
) {
    NavigationRail(
        header = {
            FloatingActionButton(
                onClick = onLog,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_nav_log_add),
                    contentDescription = stringResource(R.string.nav_log_cd),
                )
            }
        },
    ) {
        Destination.entries.forEach { dest ->
            NavigationRailItem(
                selected = current == dest,
                onClick = { onSelect(dest) },
                icon = { Icon(painterResource(dest.iconRes), contentDescription = null) },
                label = { Text(stringResource(dest.labelRes)) },
            )
        }
    }
}

private fun NavHostController.navigateTopLevel(dest: Destination) {
    navigate(dest.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
