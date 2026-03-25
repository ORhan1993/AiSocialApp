package com.bozgeyik.aisocialapp.ui.theme

import androidx.compose.ui.graphics.Color

// --- Ortak Renkler (Her iki modda da kullanılabilir) ---
val CoralPink = Color(0xFFFF6B6B)    // Ana vurgu rengimiz (Butonlar, Linkler)
val PeachOrange = Color(0xFFFF8E53)  // Vurgu gradyanı için ikinci renk
val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)

// --- 1. Soft Pastel Gündüz Modu Renkleri ---
// Arka plan gradyanı için pastel tonlar
val SoftYellowBG = Color(0xFFFFF8E1)
val SoftPinkBG = Color(0xFFFFEBEB)
val SoftBlueBG = Color(0xFFE8F1F2)

// Temel bileşen renkleri (Material3)
val PastelPrimary = CoralPink
val PastelOnPrimary = Color.White
val PastelSecondary = PeachOrange
val PastelOnSecondary = Color.White

// Metinler ve İkonlar için okunabilir griler
val TextPrimaryTitle = Color(0xFF333333) // Çok koyu gri (Başlıklar)
val TextSecondaryInput = Color(0xFF666666) // Orta gri (İpuçları, alt metinler)
val TextDisabled = Color(0xFF999999)     // Silik gri

// Giriş kutuları ve paneller için saydam beyazlar
val GlassPanelBG = Color.White.copy(alpha = 0.4f)
val GlassInputBorderFocused = Color.White.copy(alpha = 0.7f)
val GlassInputBorderUnfocused = Color.White.copy(alpha = 0.5f)


// --- 2. Soft Karanlık Gece Modu Renkleri ---
// Gece modu için ana arka plan (Tam siyah değil, yumuşak bir lacivert-gri)
val SoftDarkBG = Color(0xFF121826) // Yumuşak Gece Mavisi
val SoftDarkPanel = Color(0xFF1E2536) // Paneller için biraz daha açık ton

// Temel bileşen renkleri (Gece modunda daha soft vurgular)
val DarkPrimary = Color(0xFFFFA07A) // Soft Somon (Vurgu)
val DarkOnPrimary = Color(0xFF202020) // Koyu gri metin (Açık renk buton üstünde)
val DarkSecondary = Color(0xFFFFBD9B) // Daha soft şeftali
val DarkOnSecondary = Color(0xFF202020)

// Metinler ve İkonlar için yumuşak açık griler
val TextPrimaryDark = Color(0xFFECECEC) // Çok açık gri (Başlıklar)
val TextSecondaryDark = Color(0xFFB0B0B0) // Orta-açık gri (İpuçları)

// Gece modu panelleri ve giriş kutuları için saydam koyular
val GlassPanelBGDark = SoftDarkPanel.copy(alpha = 0.7f)
val GlassInputBorderFocusedDark = SoftDarkPanel.copy(alpha = 0.9f)
val GlassInputBorderUnfocusedDark = SoftDarkPanel.copy(alpha = 0.6f)