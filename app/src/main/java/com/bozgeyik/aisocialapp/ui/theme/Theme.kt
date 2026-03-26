package com.bozgeyik.aisocialapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// --- Gece Modu Renk Şeması (Soft Dark) ---
// (Daha önce tanımladığımız renkler korunuyor)
private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    background = SoftDarkBG,
    surface = SoftDarkPanel,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
)

// --- Gündüz Modu Renk Şeması (Soft Pastel) ---
// (Daha önce tanımladığımız renkler korunuyor)
private val LightColorScheme = lightColorScheme(
    primary = PastelPrimary,
    onPrimary = PastelOnPrimary,
    secondary = PastelSecondary,
    onSecondary = PastelOnSecondary,
    background = SoftPinkBG,
    surface = Color.White,
    onBackground = TextPrimaryTitle,
    onSurface = TextPrimaryTitle,
)

@Composable
fun AiSocialAppTheme(
    // Değiştirilen Parametre: Artık doğrudan ViewModel'deki tercihi alıyoruz
    themePreference: ThemePreference = ThemePreference.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Merkezi Mantık: Kullanıcının tercihine göre hangi renk şemasının kullanılacağını belirle
    val isSystemDark = isSystemInDarkTheme()
    val isDarkTheme = when (themePreference) {
        ThemePreference.SYSTEM -> isSystemDark
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
    }

    val colorScheme = if (isDarkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()

            // İkonların rengini temanın arka planına göre ayarla
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}