package com.example.carenest.core.presentation.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class CareNestSpacing(
    val none: Dp = 0.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 20.dp,
    val x2: Dp = 24.dp,
    val x3: Dp = 32.dp,
    val x4: Dp = 40.dp,
    val x5: Dp = 48.dp,
    val x6: Dp = 56.dp,
    val x7: Dp = 64.dp,
    val x8: Dp = 80.dp
)

data class CareNestRadius(
    val sm: Dp = 4.dp,
    val md: Dp = 8.dp,
    val lg: Dp = 12.dp,
    val xl: Dp = 16.dp,
    val x2: Dp = 24.dp,
    val full: Dp = 999.dp
)

data class CareNestElevation(
    val sm: Dp = 1.dp,
    val md: Dp = 3.dp,
    val lg: Dp = 6.dp
)

val AppSpacing = CareNestSpacing()
val AppRadius = CareNestRadius()
val AppElevation = CareNestElevation()

val LocalCareNestSpacing = staticCompositionLocalOf { CareNestSpacing() }
val LocalCareNestRadius = staticCompositionLocalOf { CareNestRadius() }
val LocalCareNestElevation = staticCompositionLocalOf { CareNestElevation() }
