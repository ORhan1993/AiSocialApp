package com.bozgeyik.aisocialapp.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.realtime.Realtime
import io.ktor.http.ContentType.Application.Json
import kotlinx.serialization.json.Json


object SupabaseClient {
    // 1. Adımda aldığın URL ve ANON KEY'i buraya tırnak içine yapıştır
    private const val SUPABASE_URL = "https://qfwzxvshjptfflduqqkp.supabase.co"
    private const val SUPABASE_KEY = "sb_publishable_cNx5LWiyB5gPh7DZNo1atA_cS59vz9J"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Auth)    // Auth
        // Postgrest (Veritabanı) ayarını böyle yapmalısın:
        install(Postgrest) {
            serializer = KotlinXSerializer(Json {
                ignoreUnknownKeys = true // Hataları önleyen sihirli ayar
                encodeDefaults = true
            })
        }
        install(Storage)   // Dosya Yükleme
        install(Realtime)

    }
}