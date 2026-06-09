package com.gymapp.domain.intent

import com.gymapp.data.model.TimeRange
import com.gymapp.domain.model.AppLanguage
import com.gymapp.domain.model.ThemeMode

/** User actions per screen (the "I" in MVI). */

sealed interface LoginIntent {
    data class UpdateMemberId(val value: String) : LoginIntent
    data class UpdatePassword(val value: String) : LoginIntent
    data object TogglePasswordVisibility : LoginIntent
    data object Submit : LoginIntent
}

sealed interface HomeIntent {
    data object Load : HomeIntent
    data object Retry : HomeIntent
}

sealed interface CheckInIntent {
    data object Load : CheckInIntent
    data object Retry : CheckInIntent
    data object Scan : CheckInIntent
}

sealed interface MembershipIntent {
    data object Load : MembershipIntent
    data object Retry : MembershipIntent
    data object ToggleFreeze : MembershipIntent
}

sealed interface ProgressIntent {
    data object Load : ProgressIntent
    data object Retry : ProgressIntent
    data class SetRange(val range: TimeRange) : ProgressIntent
}

sealed interface ProfileIntent {
    data object Load : ProfileIntent
    data object Retry : ProfileIntent
    data class SetTheme(val mode: ThemeMode) : ProfileIntent
    data class SetLanguage(val language: AppLanguage) : ProfileIntent
    data object SignOut : ProfileIntent
}
