package com.bozgeyik.aisocialapp.ui.theme

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

// Koyu Tema Renkleri Tanımlıyoruz
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark
)

// Açık Tema Renkleri Tanımlıyoruz
private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    background = BackgroundLight,
    surface = SurfaceLight,
    onSurface = Color(0xFF1C1B1F)
)

@Composable
fun AiSocialAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Telefonun ayarını otomatik algılar
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Bildirim çubuğunu (Status Bar) tema rengine boyar
            window.statusBarColor = colorScheme.background.toArgb()
            // İkonların rengini ayarlar (Koyu arka planda açık ikonlar vb.)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Typography dosyan varsa onu kullanır, yoksa varsayılan
        content = content
    )
}