package com.gymapp.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.gymapp.R

/** The four bottom-bar tabs. The center "+" (Log) is a raised action, not a tab — see [Routes]. */
enum class Destination(
    val route: String,
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int,
) {
    HOME("home", R.string.nav_home, R.drawable.ic_nav_home),
    PROGRESS("progress", R.string.nav_progress, R.drawable.ic_nav_progress),
    ACTIVITY("activity", R.string.nav_activity, R.drawable.ic_nav_activity),
    PROFILE("profile", R.string.nav_profile, R.drawable.ic_nav_profile);

    companion object {
        val START = HOME
        fun fromRoute(route: String?): Destination =
            entries.firstOrNull { it.route == route } ?: START
    }
}

/** Non-tab routes: the center Log action and the detail screens reached from Home. */
object Routes {
    const val LOG = "log"
    const val MEMBERSHIP = "membership"
    const val CHECKIN = "checkin"

    /** Detail/overlay routes hide the bottom bar and show a back arrow instead. */
    val detailRoutes = setOf(LOG, MEMBERSHIP, CHECKIN)
}
