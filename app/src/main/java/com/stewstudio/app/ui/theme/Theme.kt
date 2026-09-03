package com.stewstudio.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val StewDarkColorScheme = darkColorScheme(
    primary = Color(0xFF4A9EFF),
    onPrimary = Color.White,

    secondary = Color(0xFFB8C7D9),
    onSecondary = Color(0xFF101216),

    tertiary = Color(0xFF7CB8FF),
    onTertiary = Color(0xFF101216),

    background = Color(0xFF191A1C),
    onBackground = Color(0xFFE4E5E7),

    surface = Color(0xFF202124),
    onSurface = Color(0xFFE4E5E7),

    surfaceVariant = Color(0xFF2A2C30),
    onSurfaceVariant = Color(0xFFBFC1C5),

    outline = Color(0xFF45474C)
)

@Composable
fun StewTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StewDarkColorScheme,
        typography = Typography,
        content = content
    )
}