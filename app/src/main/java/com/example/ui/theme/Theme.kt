package com.example.ui.theme

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.model.AppThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryCyan,
    onPrimary = DarkBackground,
    primaryContainer = DarkSurfaceVariant,
    onPrimaryContainer = TextPrimary,
    secondary = PrimaryPurple,
    onSecondary = TextPrimary,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryPurple,
    onPrimary = LightSurface,
    primaryContainer = LightSurfaceVariant,
    onPrimaryContainer = LightTextPrimary,
    secondary = PrimaryCyan,
    onSecondary = LightTextPrimary,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder
)

@Composable
fun LetoVPNTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemInDark = isSystemInDarkTheme()

    val targetColorScheme: ColorScheme = when (themeMode) {
        AppThemeMode.DARK -> DarkColorScheme
        AppThemeMode.LIGHT -> LightColorScheme
        AppThemeMode.DYNAMIC -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (systemInDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (systemInDark) DarkColorScheme else LightColorScheme
            }
        }
    }

    // Animate theme transition colors smoothly
    val animatedColorScheme = targetColorScheme.copy(
        primary = animateColorAsState(targetColorScheme.primary, animationSpec = tween(400), label = "primary").value,
        onPrimary = animateColorAsState(targetColorScheme.onPrimary, animationSpec = tween(400), label = "onPrimary").value,
        primaryContainer = animateColorAsState(targetColorScheme.primaryContainer, animationSpec = tween(400), label = "primaryContainer").value,
        onPrimaryContainer = animateColorAsState(targetColorScheme.onPrimaryContainer, animationSpec = tween(400), label = "onPrimaryContainer").value,
        secondary = animateColorAsState(targetColorScheme.secondary, animationSpec = tween(400), label = "secondary").value,
        onSecondary = animateColorAsState(targetColorScheme.onSecondary, animationSpec = tween(400), label = "onSecondary").value,
        background = animateColorAsState(targetColorScheme.background, animationSpec = tween(400), label = "background").value,
        onBackground = animateColorAsState(targetColorScheme.onBackground, animationSpec = tween(400), label = "onBackground").value,
        surface = animateColorAsState(targetColorScheme.surface, animationSpec = tween(400), label = "surface").value,
        onSurface = animateColorAsState(targetColorScheme.onSurface, animationSpec = tween(400), label = "onSurface").value,
        surfaceVariant = animateColorAsState(targetColorScheme.surfaceVariant, animationSpec = tween(400), label = "surfaceVariant").value,
        onSurfaceVariant = animateColorAsState(targetColorScheme.onSurfaceVariant, animationSpec = tween(400), label = "onSurfaceVariant").value,
        outline = animateColorAsState(targetColorScheme.outline, animationSpec = tween(400), label = "outline").value
    )

    MaterialTheme(
        colorScheme = animatedColorScheme,
        typography = Typography,
        content = content
    )
}
