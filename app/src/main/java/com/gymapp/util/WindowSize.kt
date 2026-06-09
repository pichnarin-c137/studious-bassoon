package com.gymapp.util

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * App-level breakpoint, derived from Material 3's [WindowWidthSizeClass] (spec table):
 * - COMPACT  (< 600dp)  phones — primary target
 * - MEDIUM   (600–840)  large phones / small tablets
 * - EXPANDED (> 840)    tablets / foldables
 */
enum class WindowSize { COMPACT, MEDIUM, EXPANDED }

fun WindowWidthSizeClass.toWindowSize(): WindowSize = when (this) {
    WindowWidthSizeClass.Compact -> WindowSize.COMPACT
    WindowWidthSizeClass.Medium -> WindowSize.MEDIUM
    else -> WindowSize.EXPANDED
}

/** True when navigation should use a side rail + multi-column layouts instead of a bottom bar. */
val WindowSize.isExpanded: Boolean get() = this == WindowSize.EXPANDED

/** Provided once at the root so any composable can adapt without prop-drilling. */
val LocalWindowSize = staticCompositionLocalOf { WindowSize.COMPACT }
