package com.gymapp.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Performance aesthetic: a near-black canvas with a single electric-lime accent. Lime is reserved
 * for the next action, live/active status, and positive progress — never decorative. Text on lime
 * is the near-black canvas tone (dark olive is the other allowed option), never pure black.
 */
val Lime = Color(0xFFD6FB3D)
private val OnLime = Color(0xFF101012)

// Dark (primary) palette
private val Canvas = Color(0xFF101012)      // background / surface
private val Raised = Color(0xFF1A1A1E)      // cards & raised surfaces
private val Border = Color(0xFF2A2A2E)      // subtle borders / dividers
private val TextHigh = Color(0xFFECECEE)    // primary text
private val TextMuted = Color(0xFF8A8A92)   // secondary text & labels

// Amber warning hue, used only for the near-expiry / renewal-nudge membership state.
private val Warning = Color(0xFFE5A53D)

val DarkColors = darkColorScheme(
    primary = Lime,
    onPrimary = OnLime,
    primaryContainer = Color(0xFF26330A),
    onPrimaryContainer = Lime,
    secondary = TextMuted,
    onSecondary = Canvas,
    secondaryContainer = Raised,
    onSecondaryContainer = TextHigh,
    tertiary = Warning,
    onTertiary = Color(0xFF2A1B00),
    tertiaryContainer = Color(0xFF3A2A00),
    onTertiaryContainer = Color(0xFFFFDDA8),
    background = Canvas,
    onBackground = TextHigh,
    surface = Canvas,
    onSurface = TextHigh,
    surfaceVariant = Raised,
    onSurfaceVariant = TextMuted,
    outline = Border,
    outlineVariant = Border,
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF2A0000),
    errorContainer = Color(0xFF3A1212),
    onErrorContainer = Color(0xFFFFD9D9),
)

// Light variant (secondary): same single lime accent on clean light surfaces.
private val LightCanvas = Color(0xFFFAFAFB)
private val LightRaised = Color(0xFFFFFFFF)
private val LightBorder = Color(0xFFE3E3E7)
private val LightTextHigh = Color(0xFF17171A)
private val LightTextMuted = Color(0xFF6A6A72)

val LightColors = lightColorScheme(
    primary = Lime,
    onPrimary = OnLime,
    primaryContainer = Color(0xFFE9FBA8),
    onPrimaryContainer = Color(0xFF2C3A00),
    secondary = LightTextMuted,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFEDEDF0),
    onSecondaryContainer = LightTextHigh,
    tertiary = Color(0xFF9A6B00),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFE3B0),
    onTertiaryContainer = Color(0xFF2A1B00),
    background = LightCanvas,
    onBackground = LightTextHigh,
    surface = LightCanvas,
    onSurface = LightTextHigh,
    surfaceVariant = LightRaised,
    onSurfaceVariant = LightTextMuted,
    outline = LightBorder,
    outlineVariant = LightBorder,
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)
