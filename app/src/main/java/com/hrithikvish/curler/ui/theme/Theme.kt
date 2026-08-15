package com.hrithikvish.curler.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ---------------------------------------------------------------------
// LIGHT COLOR SCHEME
// ---------------------------------------------------------------------

val CurlerLightColorScheme = lightColorScheme(
    primary = Black90,
    onPrimary = White,
    primaryContainer = Black05,
    onPrimaryContainer = Black90,
    inversePrimary = Black10,

    secondary = Black60,
    onSecondary = White,
    secondaryContainer = Black05,
    onSecondaryContainer = Black80,

    tertiary = Black50,
    onTertiary = White,
    tertiaryContainer = Black10,
    onTertiaryContainer = Black90,

    background = White,
    onBackground = Black90,

    surface = White,
    onSurface = Black90,
    surfaceVariant = Black05,
    onSurfaceVariant = Black60,
    surfaceTint = Black90,

    surfaceBright = White,
    surfaceDim = Black05,
    surfaceContainerLowest = White,
    surfaceContainerLow = Black02,
    surfaceContainer = Black05,
    surfaceContainerHigh = Black10,
    surfaceContainerHighest = Black20,

    inverseSurface = Black90,
    inverseOnSurface = White,

    error = SignalError,
    onError = White,
    errorContainer = Color(0xFFFCE4E4),
    onErrorContainer = Color(0xFF7A0000),

    outline = Black30,
    outlineVariant = Black10,
    scrim = Black100,
)

// ---------------------------------------------------------------------
// DARK COLOR SCHEME — a true inversion, not a separate palette
// ---------------------------------------------------------------------

val CurlerDarkColorScheme = darkColorScheme(
    primary = White,
    onPrimary = Black90,
    primaryContainer = Black80,
    onPrimaryContainer = Black05,
    inversePrimary = Black80,

    secondary = Black20,
    onSecondary = Black90,
    secondaryContainer = Black80,
    onSecondaryContainer = Black05,

    tertiary = Black30,
    onTertiary = Black90,
    tertiaryContainer = Black70,
    onTertiaryContainer = Black05,

    background = Black100,
    onBackground = White,

    surface = Black95,
    onSurface = White,
    surfaceVariant = Black80,
    onSurfaceVariant = Black30,
    surfaceTint = White,

    surfaceBright = Black70,
    surfaceDim = Black100,
    surfaceContainerLowest = Black100,
    surfaceContainerLow = Black95,
    surfaceContainer = Black90,
    surfaceContainerHigh = Black80,
    surfaceContainerHighest = Black70,

    inverseSurface = White,
    inverseOnSurface = Black90,

    error = Color(0xFFEF9A9A),
    onError = Color(0xFF680000),
    errorContainer = Color(0xFF930000),
    onErrorContainer = Color(0xFFFFDAD6),

    outline = Black50,
    outlineVariant = Black70,
    scrim = Black100,
)

// ---------------------------------------------------------------------
// THEME COMPOSABLE
//    dynamicColor is forced off by default — Material You would inject
//    the user's wallpaper hue into "primary", which breaks the
//    monochrome brief.
// ---------------------------------------------------------------------

@Composable
fun CurlerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> CurlerDarkColorScheme
        else -> CurlerLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CurlerTypography,
        shapes = CurlerShapes,
        content = content,
    )
}
