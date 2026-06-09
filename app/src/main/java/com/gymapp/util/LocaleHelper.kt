package com.gymapp.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.gymapp.domain.model.AppLanguage

/** Thin wrapper over the AndroidX per-app locale API. */
object LocaleHelper {

    /** Apply [language] immediately; AppCompat persists and restores it across launches. */
    fun apply(language: AppLanguage) {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(language.code),
        )
    }

    /** The currently applied app language, or [AppLanguage.ENGLISH] if none was set. */
    fun currentLanguage(): AppLanguage {
        val locales = AppCompatDelegate.getApplicationLocales()
        return if (locales.isEmpty) AppLanguage.ENGLISH else AppLanguage.from(locales[0]?.language)
    }
}
