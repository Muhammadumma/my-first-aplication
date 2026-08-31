package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ClearanceColorScheme = lightColorScheme(
    primary = ClearancePrimary,
    onPrimary = ClearanceOnPrimary,
    primaryContainer = ClearancePrimaryContainer,
    onPrimaryContainer = ClearanceOnPrimaryContainer,
    secondary = ClearanceSecondary,
    onSecondary = ClearanceOnSecondary,
    secondaryContainer = ClearanceSecondaryContainer,
    onSecondaryContainer = ClearanceOnSecondaryContainer,
    tertiary = ClearanceTertiary,
    onTertiary = ClearanceOnTertiary,
    tertiaryContainer = ClearanceTertiaryContainer,
    onTertiaryContainer = ClearanceOnTertiaryContainer,
    background = ClearanceBackground,
    onBackground = ClearanceOnSurface,
    surface = ClearanceSurface,
    onSurface = ClearanceOnSurface,
    surfaceVariant = ClearanceSurfaceContainerHigh,
    onSurfaceVariant = ClearanceOnSurfaceVariant,
    surfaceContainer = ClearanceSurfaceContainer,
    surfaceContainerHigh = ClearanceSurfaceContainerHigh,
    surfaceContainerHighest = ClearanceSurfaceContainerHighest,
    surfaceContainerLow = ClearanceSurfaceContainerLow,
    surfaceContainerLowest = ClearanceSurfaceContainerLowest,
    surfaceBright = ClearanceSurfaceBright,
    error = ClearanceError,
    onError = ClearanceOnError,
    errorContainer = ClearanceErrorContainer,
    onErrorContainer = ClearanceOnErrorContainer,
    outline = ClearanceOutline,
    outlineVariant = ClearanceOutlineVariant,
    inverseSurface = ClearanceInverseSurface,
    inverseOnSurface = ClearanceInverseOnSurface
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ClearanceColorScheme,
        typography = Typography,
        content = content
    )
}
