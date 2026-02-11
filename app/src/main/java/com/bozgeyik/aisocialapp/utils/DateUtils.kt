package com.bozgeyik.aisocialapp.utils

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object DateUtils {
    fun timeAgo(dateString: String?): String {
        if (dateString == null) return ""
        try {
            // Supabase tarih formatı (ISO 8601)
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            format.timeZone = TimeZone.getTimeZone("UTC")
            val date = format.parse(dateString) ?: return ""

            val now = System.currentTimeMillis()
            val diff = now - date.time

            val seconds = diff / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24

            return when {
                seconds < 60 -> "Az önce"
                minutes < 60 -> "$minutes dk önce"
                hours < 24 -> "$hours sa önce"
                days < 7 -> "$days gün önce"
                else -> SimpleDateFormat("dd MMM", Locale("tr")).format(date)
            }
        } catch (e: Exception) {
            return ""
        }
    }
}