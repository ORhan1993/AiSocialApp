package com.bozgeyik.aisocialapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.Color

// --- Gece Modu Renk Şeması (Soft Dark) ---
private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    background = SoftDarkBG,
    surface = SoftDarkPanel,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    // Diğer renkleri de gerektiğinde buraya ekleyebilirsin
)

// --- Gündüz Modu Renk Şeması (Soft Pastel) ---
private val LightColorScheme = lightColorScheme(
    primary = PastelPrimary,
    onPrimary = PastelOnPrimary,
    secondary = PastelSecondary,
    onSecondary = PastelOnSecondary,
    background = SoftPinkBG, // Varsayılan arka plan (Gradyan kullanmayacaksan)
    surface = Color.White,
    onBackground = TextPrimaryTitle,
    onSurface = TextPrimaryTitle,
    // Diğer renkleri de gerektiğinde buraya ekleyebilirsin
)

@Composable
fun AiSocialAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dinamik renkler Android 12+ için varsayılan olarak kapalı,
    // kendi özel renk paletimizi kullanmak istiyoruz.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        //     val context = LocalContext.current
        //     if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        // }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Durum çubuğu (Status Bar) rengini temanın arka planına göre ayarla
            window.statusBarColor = colorScheme.background.toArgb()
            // Durum çubuğundaki ikonların rengini (Koyu/Açık) ayarla
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Typography.kt dosyasındaki varsayılan tipografi
        content = content
    )
}