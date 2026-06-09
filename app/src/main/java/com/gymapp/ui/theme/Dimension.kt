package com.gymapp.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gymapp.util.WindowSize

/** Responsive sizes that change with the window breakpoint. */
object Dimension {
    /** QR code edge length (spec: full-screen on phones, smaller on bigger screens). */
    fun qrSize(window: WindowSize): Dp = when (window) {
        WindowSize.COMPACT -> 240.dp
        WindowSize.MEDIUM -> 280.dp
        WindowSize.EXPANDED -> 320.dp
    }

    /** Max content width so text columns don't stretch uncomfortably wide on tablets. */
    fun contentMaxWidth(window: WindowSize): Dp = when (window) {
        WindowSize.EXPANDED -> 1040.dp
        else -> Dp.Infinity
    }

    val cardCorner: Dp = 20.dp
    val statCardHeight: Dp = 96.dp
}
