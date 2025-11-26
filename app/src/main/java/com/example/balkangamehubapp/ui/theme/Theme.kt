package com.example.balkangamehubapp.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,

    // 🟣 DODANO — pravi dark theme
    background = Color(0xFF0D0D0D),
    surface = Color(0xFF0D0D0D),

    onBackground = Color(0xFFE6E6E6),
    onSurface = Color(0xFFE6E6E6),
    onPrimary = Color.White
)

@Composable
fun BalkanGameHubAppTheme(
    darkTheme: Boolean = true, // 🟢 uvijek dark
    dynamicColor: Boolean = false, // ❌ isključeno
    content: @Composable () -> Unit
) {
    val colorScheme =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && dynamicColor) {
            dynamicDarkColorScheme(LocalContext.current)
        } else {
            DarkColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
