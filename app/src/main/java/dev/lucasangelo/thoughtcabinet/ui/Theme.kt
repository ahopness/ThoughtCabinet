package dev.lucasangelo.thoughtcabinet.ui

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Surface
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1C1B1F),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF2F2F2),
    onPrimaryContainer = Color(0xFF1C1B1F),
    secondary = Color(0xFF49454F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8E8E8),
    onSecondaryContainer = Color(0xFF1C1B1F),
    tertiary = Color(0xFF605D62),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF0F0F0),
    onTertiaryContainer = Color(0xFF1C1B1F),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFE0E0E0),
    onPrimary = Color(0xFF1C1B1F),
    primaryContainer = Color(0xFF121212),
    onPrimaryContainer = Color(0xFFF2F2F2),
    secondary = Color(0xFFB8B8B8),
    onSecondary = Color(0xFF1C1B1F),
    secondaryContainer = Color(0xFF3F3F3F),
    onSecondaryContainer = Color(0xFFF2F2F2),
    tertiary = Color(0xFF9E9E9E),
    onTertiary = Color(0xFF1C1B1F),
    tertiaryContainer = Color(0xFF363636),
    onTertiaryContainer = Color(0xFFF2F2F2),
    background = Color(0xFF000000),
    onBackground = Color(0xFFC0C0C0),
    surface = Color(0xFF000000),
    onSurface = Color(0xFFC0C0C0),
    surfaceVariant = Color(0xFF2B2B2B),
    onSurfaceVariant = Color(0xFFC7C7C7),
    outline = Color(0xFF8A8A8A),
)

@Composable
fun ThoughtCabinetTheme(
    content: @Composable () -> Unit
) {
    val darkTheme = isSystemInDarkTheme()

    val view = LocalView.current
    val activity = view.context as Activity
    val windowInsetsController = WindowCompat.getInsetsController(activity.window, view)
    windowInsetsController.isAppearanceLightStatusBars = !darkTheme
    windowInsetsController.isAppearanceLightNavigationBars = !darkTheme

    // NOTE: too much of a hassle to implement
//    val colorScheme = if(!darkTheme) LightColorScheme else DarkColorScheme
    val colorScheme = DarkColorScheme

    val rippleConfiguration = RippleConfiguration(
        color = if (!darkTheme) Color.Black else Color.White,
        rippleAlpha = RippleAlpha(
            pressedAlpha = 0.10f,
            focusedAlpha = 0.12f,
            draggedAlpha = 0.08f,
            hoveredAlpha = 0.04f,
        )
    )

    // NOTE: i made the whole app with the emulator's smaller screen
    // the dpi looked too big on actual devices, scaling it down so it looks ok
    val currentDensity = LocalDensity.current
    val customDensity = Density(
        density = currentDensity.density * 0.85f,
        fontScale = currentDensity.fontScale
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
    ) {
        CompositionLocalProvider(
            LocalRippleConfiguration provides rippleConfiguration,
            LocalDensity provides customDensity
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                content()
            }
        }
    }
}