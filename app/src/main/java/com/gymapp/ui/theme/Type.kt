package com.gymapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.gymapp.R

// Noto Sans Khmer covers both Khmer and basic Latin, so it is the single app font family —
// guaranteeing correct rendering whether the UI is in English or ភាសាខ្មែរ.
val NotoSansKhmer = FontFamily(Font(R.font.noto_sans_khmer))

// Stats / scores / PRs render in the system monospace so digits align like a scoreboard
// (the "instrument panel" feel). Numbers are Latin digits, so this is Khmer-safe and dep-free.
val MonoNumbers = FontFamily.Monospace

private val base = Typography()

val GymTypography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = NotoSansKhmer),
    displayMedium = base.displayMedium.copy(fontFamily = NotoSansKhmer),
    displaySmall = base.displaySmall.copy(fontFamily = NotoSansKhmer),
    headlineLarge = base.headlineLarge.copy(fontFamily = NotoSansKhmer),
    headlineMedium = base.headlineMedium.copy(fontFamily = NotoSansKhmer),
    headlineSmall = base.headlineSmall.copy(fontFamily = NotoSansKhmer),
    titleLarge = base.titleLarge.copy(fontFamily = NotoSansKhmer),
    titleMedium = base.titleMedium.copy(fontFamily = NotoSansKhmer),
    titleSmall = base.titleSmall.copy(fontFamily = NotoSansKhmer),
    bodyLarge = base.bodyLarge.copy(fontFamily = NotoSansKhmer),
    bodyMedium = base.bodyMedium.copy(fontFamily = NotoSansKhmer),
    bodySmall = base.bodySmall.copy(fontFamily = NotoSansKhmer),
    labelLarge = base.labelLarge.copy(fontFamily = NotoSansKhmer),
    labelMedium = base.labelMedium.copy(fontFamily = NotoSansKhmer),
    labelSmall = base.labelSmall.copy(fontFamily = NotoSansKhmer),
)
