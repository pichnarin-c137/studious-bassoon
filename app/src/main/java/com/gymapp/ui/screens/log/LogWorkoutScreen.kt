package com.gymapp.ui.screens.log

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gymapp.R
import com.gymapp.ui.components.ComingSoon

/** Template-based workout logging (pick a routine, log sets) — built in a later pass. */
@Composable
fun LogWorkoutScreen() {
    ComingSoon(
        iconRes = R.drawable.ic_nav_log_add,
        body = stringResource(R.string.log_coming_soon),
    )
}
