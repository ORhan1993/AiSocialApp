package com.bozgeyik.aisocialapp.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.storage.Storage
import kotlinx.serialization.json.Json

object SupabaseClient {
    private const val SUPABASE_URL = "https://qfwzxvshjptfflduqqkp.supabase.co"
    private const val SUPABASE_KEY = "sb_publishable_cNx5LWiyB5gPh7DZNo1atA_cS59vz9J"

    // YENİ VE GÜVENLİ YÖNTEM: İstemci, ilk erişildiğinde sadece bir kez oluşturulur.
    val client by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_KEY
        ) {
            install(Auth)
            install(Postgrest) {
                serializer = KotlinXSerializer(Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                })
            }
            install(Storage)
            install(Realtime)
        }
    }
}