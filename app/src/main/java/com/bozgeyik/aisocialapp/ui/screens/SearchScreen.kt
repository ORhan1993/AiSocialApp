package com.bozgeyik.aisocialapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.bozgeyik.aisocialapp.data.Friendship
import com.bozgeyik.aisocialapp.data.Profile
import com.bozgeyik.aisocialapp.data.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(navController: NavController) {
    var query by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Profile>>(emptyList()) }

    // Arama değiştikçe veritabanından getir
    LaunchedEffect(query) {
        if (query.isNotEmpty()) {
            try {
                // 'username' sütununda arama yap (benzer olanları getir)
                searchResults = SupabaseClient.client.from("profiles")
                    .select {
                        filter { ilike("username", "%$query%") }
                    }.decodeList<Profile>()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Arama Çubuğu
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Kullanıcı Ara") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Sonuç Listesi
        LazyColumn {
            items(searchResults) { profile ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { navController.navigate("chat/${profile.username}") },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profil Resmi (Varsa)
                    Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.Gray)) {
                        if (profile.avatar_url != null) {
                            AsyncImage(
                                model = profile.avatar_url,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                profile.username.take(1).uppercase(),
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = profile.username,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // ARKADAŞ EKLE BUTONU (Yeni)
                    val scope = rememberCoroutineScope()
                    val context = LocalContext.current
                    var requestSent by remember { mutableStateOf(false) }

                    if (!requestSent) {
                        Button(
                            onClick = {
                                scope.launch {
                                    val currentUser =
                                        SupabaseClient.client.auth.currentUserOrNull()?.email?.split(
                                            "@"
                                        )?.get(0) ?: ""
                                    val friendship = Friendship(
                                        sender_username = currentUser,
                                        receiver_username = profile.username,
                                        status = "pending"
                                    )
                                    // İstek gönder
                                    try {
                                        SupabaseClient.client.from("friendships").insert(friendship)
                                        requestSent = true
                                        Toast.makeText(
                                            context,
                                            "İstek yollandı",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } catch (e: Exception) {
                                        // Zaten ekliyse hata verir
                                    }
                                }
                            },
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Ekle", fontSize = 12.sp)
                        }
                    } else {
                        Text("İstek yollandı", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}