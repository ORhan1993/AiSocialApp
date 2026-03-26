package com.bozgeyik.aisocialapp.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

// Kullanıcının tema tercihini temsil eden Enum
enum class ThemePreference(val label: String) {
    SYSTEM("Sistem Varsayılanı"),
    LIGHT("Gündüz Modu"),
    DARK("Gece Modu")
}

class ThemeViewModel(context: Context) : ViewModel() {

    // Kullanıcının tercihini sakladığımız Shared Preferences
    private val prefs = context.getSharedPreferences("theme_settings", Context.MODE_PRIVATE)

    // Uygulamanın anlık tema modunu Compose'a bildiren reaktif durum
    var currentThemePreference by mutableStateOf(ThemePreference.SYSTEM)
        private set

    init {
        // ViewModel ilk oluşturulduğunda hafızadaki tercihi oku
        val savedTheme = prefs.getString("selected_theme", ThemePreference.SYSTEM.name)
        currentThemePreference = ThemePreference.valueOf(savedTheme ?: ThemePreference.SYSTEM.name)
    }

    // Kullanıcının tema tercihini değiştiren ve hafızaya kaydeden fonksiyon
    fun setThemePreference(preference: ThemePreference) {
        currentThemePreference = preference
        prefs.edit().putString("selected_theme", preference.name).apply()
    }
}