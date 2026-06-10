package com.gymapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.gymapp.domain.model.ThemeMode

private val LocalAccentInk = staticCompositionLocalOf { Lime }

/**
 * The accent used as ink — text, strokes, icons, chart lines, status dots. Electric lime on dark
 * surfaces; the darkened [LimeInk] on light ones, where pure lime has no contrast. Use this for
 * anything thin or textual; keep [ColorScheme.primary] for lime *fills* with dark content on top.
 */
val ColorScheme.accentInk: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAccentInk.current

/**
 * App theme. Resolves [themeMode] against the system setting and applies the fixed brand palette
 * in [Color.kt]. Material You dynamic color is off by default: the single electric-lime accent is
 * the brand and must not be overridden by the device wallpaper.
 */
@Composable
fun GymTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    CompositionLocalProvider(LocalAccentInk provides if (darkTheme) Lime else LimeInk) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = GymTypography,
            content = content,
        )
    }
}
