package com.example.hacaton.ui.theme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC), // A nice purple for dark theme
    secondary = Color(0xFF03DAC6),
    background = Color(0xFF121212), // Standard dark background
    surface = Color(0xFF1E1E1E), // Slightly lighter for cards
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color(0xFFE0E0E0), // Lighter text on dark background
    onSurface = Color(0xFFE0E0E0)
)

@Composable
fun HacatonTheme(
    darkTheme: Boolean = true, // Always dark
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
