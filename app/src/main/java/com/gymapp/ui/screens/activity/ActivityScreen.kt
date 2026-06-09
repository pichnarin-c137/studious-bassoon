package com.gymapp.ui.screens.activity

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gymapp.R
import com.gymapp.ui.components.ComingSoon

/** Social feed (friends' workouts, kudos, branch leaderboards) — built in a later pass. */
@Composable
fun ActivityScreen() {
    ComingSoon(
        iconRes = R.drawable.ic_nav_activity,
        body = stringResource(R.string.activity_coming_soon),
    )
}
