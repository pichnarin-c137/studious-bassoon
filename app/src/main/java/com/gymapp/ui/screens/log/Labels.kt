package com.gymapp.ui.screens.log

import androidx.annotation.StringRes
import com.gymapp.R
import com.gymapp.data.model.SessionType

@StringRes
fun SessionType.labelRes(): Int = when (this) {
    SessionType.GYM -> R.string.log_type_gym
    SessionType.CARDIO -> R.string.log_type_cardio
    SessionType.BODYWEIGHT -> R.string.log_type_bodyweight
}
