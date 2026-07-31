package com.openfinds.app.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Indigo50,
    onPrimary = Neutral0,
    primaryContainer = Indigo10,
    onPrimaryContainer = Indigo80,
    secondary = Neutral70,
    onSecondary = Neutral0,
    background = Neutral10,
    onBackground = Neutral95,
    surface = Neutral0,
    onSurface = Neutral95,
    surfaceVariant = Neutral20,
    onSurfaceVariant = Neutral70,
    outline = Neutral30,
    error = Error50,
    onError = Neutral0,
)

private val DarkColors = darkColorScheme(
    primary = Indigo40,
    onPrimary = Neutral99,
    primaryContainer = Indigo80,
    onPrimaryContainer = Indigo10,
    secondary = Neutral50,
    onSecondary = Neutral99,
    background = Neutral99,
    onBackground = Neutral20,
    surface = Neutral95,
    onSurface = Neutral20,
    surfaceVariant = Neutral85,
    onSurfaceVariant = Neutral50,
    outline = Neutral85,
    error = Error50,
    onError = Neutral99,
)

@Composable
fun OpenFindTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = OpenFindTypography,
        shapes = OpenFindShapes,
        content = content,
    )
}
