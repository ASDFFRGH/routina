package com.routina.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF3E6B4A), onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = androidx.compose.ui.graphics.Color(0xFFC0F2C8), onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF00210C),
    secondary = androidx.compose.ui.graphics.Color(0xFF55624C), onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFFD8E8CB), onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFF131F0D),
    tertiary = androidx.compose.ui.graphics.Color(0xFF38656F), onTertiary = androidx.compose.ui.graphics.Color.White,
    tertiaryContainer = androidx.compose.ui.graphics.Color(0xFFBCEAF6), onTertiaryContainer = androidx.compose.ui.graphics.Color(0xFF001F26),
    background = androidx.compose.ui.graphics.Color(0xFFF9FBF4), onBackground = androidx.compose.ui.graphics.Color(0xFF1A1C19),
    surface = androidx.compose.ui.graphics.Color(0xFFF9FBF4), onSurface = androidx.compose.ui.graphics.Color(0xFF1A1C19),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFFDEE5D8), onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF424940),
)
private val DarkColors = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFFA4D8AB), onPrimary = androidx.compose.ui.graphics.Color(0xFF10371C),
    primaryContainer = androidx.compose.ui.graphics.Color(0xFF285233), onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFFC0F2C8),
    secondary = androidx.compose.ui.graphics.Color(0xFFBECCB2), onSecondary = androidx.compose.ui.graphics.Color(0xFF283422),
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFF3E4A37), onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFFD8E8CB),
    tertiary = androidx.compose.ui.graphics.Color(0xFFA0CEDA), onTertiary = androidx.compose.ui.graphics.Color(0xFF003640),
    tertiaryContainer = androidx.compose.ui.graphics.Color(0xFF1F4D57), onTertiaryContainer = androidx.compose.ui.graphics.Color(0xFFBCEAF6),
    background = androidx.compose.ui.graphics.Color(0xFF121411), onBackground = androidx.compose.ui.graphics.Color(0xFFE2E4DD),
    surface = androidx.compose.ui.graphics.Color(0xFF121411), onSurface = androidx.compose.ui.graphics.Color(0xFFE2E4DD),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF424940), onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFC2C9BD),
)

@Composable
fun RoutinaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content,
    )
}
