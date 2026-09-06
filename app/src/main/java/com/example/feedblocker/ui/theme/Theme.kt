package com.example.feedblocker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Mint,
    onPrimary = Ink,
    secondary = MintDark,
    background = Ink,
    surface = InkSoft,
    onBackground = Color(0xFFE8EEF0),
    onSurface = Color(0xFFE8EEF0),
    onSurfaceVariant = Color(0xFF9AA7B0),
    outline = Color(0xFF3A454D),
    error = Danger
)

private val LightColorScheme = lightColorScheme(
    primary = MintDark,
    onPrimary = Color.White,
    secondary = Mint,
    background = Cloud,
    surface = CloudCard,
    onBackground = Ink,
    onSurface = Ink,
    onSurfaceVariant = Slate,
    outline = Color(0xFFD5DCD8),
    error = Danger
)

@Composable
fun FeedBlockerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
