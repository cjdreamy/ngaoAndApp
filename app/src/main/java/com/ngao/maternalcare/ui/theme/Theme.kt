package com.ngao.maternalcare.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NgaoDarkColors = darkColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    secondary = Teal,
    background = NavyBackground,
    onBackground = TextPrimary,
    surface = NavySurface,
    onSurface = TextPrimary,
    surfaceVariant = NavyCard,
    onSurfaceVariant = TextSecondary,
    error = DangerRed,
    outline = NavyBorder
)

@Composable
fun NgaoMaternalCareTheme(
    darkTheme: Boolean = true, // the web app defaults to its dark theme
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NgaoDarkColors,
        typography = Typography,
        content = content
    )
}
