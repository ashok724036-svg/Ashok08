package com.neetquest.neetquestsaver.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Custom Color Palette ──────────────────────────────────────────────────────

// Primary: Deep academic blue-violet
private val NEETViolet = Color(0xFF5C35A5)
private val NEETVioletLight = Color(0xFF7B57C4)

// Subject colors
val SubjectPhysicsColor   = Color(0xFF1565C0)  // Blue
val SubjectChemistryColor = Color(0xFF00695C)  // Teal
val SubjectBotanyColor    = Color(0xFF2E7D32)  // Green
val SubjectZoologyColor   = Color(0xFFE65100)  // Orange

private val DarkColorScheme = darkColorScheme(
    primary          = Color(0xFFB39DDB),
    onPrimary        = Color(0xFF2A1B5E),
    primaryContainer = Color(0xFF3D2A7A),
    onPrimaryContainer = Color(0xFFE8DEF8),
    secondary        = Color(0xFF80DEEA),
    onSecondary      = Color(0xFF003A40),
    secondaryContainer = Color(0xFF00525A),
    onSecondaryContainer = Color(0xFFA2F0FC),
    tertiary         = Color(0xFFF48FB1),
    background       = Color(0xFF0F0F1A),
    onBackground     = Color(0xFFE6E1E5),
    surface          = Color(0xFF1C1B2E),
    onSurface        = Color(0xFFE6E1E5),
    surfaceVariant   = Color(0xFF2A2840),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline          = Color(0xFF49455E),
    error            = Color(0xFFEF9A9A),
)

private val LightColorScheme = lightColorScheme(
    primary          = NEETViolet,
    onPrimary        = Color.White,
    primaryContainer = Color(0xFFEDE7F6),
    onPrimaryContainer = Color(0xFF1A0060),
    secondary        = Color(0xFF00838F),
    onSecondary      = Color.White,
    secondaryContainer = Color(0xFFB2EBF2),
    onSecondaryContainer = Color(0xFF00262A),
    tertiary         = Color(0xFFC2185B),
    background       = Color(0xFFF8F7FF),
    onBackground     = Color(0xFF1C1B1F),
    surface          = Color.White,
    onSurface        = Color(0xFF1C1B1F),
    surfaceVariant   = Color(0xFFEFEBF8),
    onSurfaceVariant = Color(0xFF49454F),
    outline          = Color(0xFF7A757F),
    error            = Color(0xFFB00020),
)

@Composable
fun NEETQuestSaverTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = NEETTypography,
        content = content
    )
}

fun subjectColor(subject: String): Color = when (subject) {
    "Physics"   -> SubjectPhysicsColor
    "Chemistry" -> SubjectChemistryColor
    "Botany"    -> SubjectBotanyColor
    "Zoology"   -> SubjectZoologyColor
    else        -> NEETViolet
}
