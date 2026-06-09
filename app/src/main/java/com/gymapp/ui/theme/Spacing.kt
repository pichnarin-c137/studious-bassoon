package com.gymapp.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gymapp.util.WindowSize

/** dp spacing scale. Use these instead of magic numbers so spacing stays consistent. */
object Spacing {
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 16.dp
    val lg: Dp = 24.dp
    val xl: Dp = 32.dp
    val xxl: Dp = 48.dp
}

/** Screen edge padding adapts per breakpoint (spec: 16 / 24 / 32 dp). */
fun WindowSize.screenPadding(): Dp = when (this) {
    WindowSize.COMPACT -> 16.dp
    WindowSize.MEDIUM -> 24.dp
    WindowSize.EXPANDED -> 32.dp
}
