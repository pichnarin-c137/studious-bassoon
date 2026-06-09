package com.gymapp.domain.model

/** Persisted theme choice (DataStore key `theme_mode`). */
enum class ThemeMode {
    LIGHT, DARK, SYSTEM;

    val storageValue: String get() = name.lowercase()

    companion object {
        fun from(value: String?): ThemeMode =
            entries.firstOrNull { it.storageValue == value } ?: SYSTEM
    }
}

/** Persisted language choice (DataStore key `app_language`). */
enum class AppLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    KHMER("km", "ភាសាខ្មែរ");

    companion object {
        fun from(code: String?): AppLanguage =
            entries.firstOrNull { it.code == code } ?: ENGLISH
    }
}
