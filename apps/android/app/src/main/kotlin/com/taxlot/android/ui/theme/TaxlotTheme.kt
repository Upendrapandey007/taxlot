package com.taxlot.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TaxlotColorScheme = lightColorScheme(
    primary = Color(0xFF4F46E5),        // Indigo
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    secondary = Color(0xFF6366F1),
    onSecondary = Color.White,
    background = Color(0xFFF9FAFB),
    surface = Color.White,
    error = Color(0xFFEF4444),
    onBackground = Color(0xFF111827),
    onSurface = Color(0xFF111827),
)

@Composable
fun TaxlotTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TaxlotColorScheme,
        content = content
    )
}
