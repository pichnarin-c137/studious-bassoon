package com.gymapp.ui.screens.log

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.gymapp.R
import com.gymapp.data.model.SessionType

@StringRes
fun SessionType.labelRes(): Int = when (this) {
    SessionType.GYM -> R.string.log_type_gym
    SessionType.CARDIO -> R.string.log_type_cardio
    SessionType.BODYWEIGHT -> R.string.log_type_bodyweight
}

/** Tinted accent glyph per quick-log type — gives the primary "what did you train?" choice weight. */
@DrawableRes
fun SessionType.iconRes(): Int = when (this) {
    SessionType.GYM -> R.drawable.ic_barbell
    SessionType.CARDIO -> R.drawable.ic_flame
    SessionType.BODYWEIGHT -> R.drawable.ic_bodyweight
}
