package com.bozgeyik.aisocialapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bozgeyik.aisocialapp.R // R.drawable.ic_app_logo için gerekli
import com.bozgeyik.aisocialapp.data.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateToHome: () -> Unit, onNavigateToLogin: () -> Unit) {

    LaunchedEffect(Unit) {
        // Logoyu 1.5 saniye göster (Marka bilinirliği + Yükleme süresi)
        delay(1500)

        // Session (Oturum) kontrolü
        val session = SupabaseClient.client.auth.currentSessionOrNull()
        if (session != null) {
            onNavigateToHome() // Oturum varsa Ana Sayfaya
        } else {
            onNavigateToLogin() // Yoksa Giriş Sayfasına
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // Logonun olduğu yer (vector asset ismin neyse onu yaz)
        Image(
            painter = painterResource(id = R.drawable.ic_app_logo),
            contentDescription = "Logo",
            modifier = Modifier.size(150.dp)
        )
    }
}