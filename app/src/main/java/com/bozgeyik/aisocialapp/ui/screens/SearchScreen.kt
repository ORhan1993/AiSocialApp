package com.bozgeyik.aisocialapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.bozgeyik.aisocialapp.data.Friendship
import com.bozgeyik.aisocialapp.data.Profile
import com.bozgeyik.aisocialapp.data.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavController) {
    // --- STATE TANIMLAMALARI ---
    var query by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) } // Arama çubuğu açık mı?
    var searchResults by remember { mutableStateOf<List<Profile>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Şu anki kullanıcı
    val currentUser = SupabaseClient.client.auth.currentUserOrNull()
    val currentUsername = currentUser?.email?.split("@")?.get(0) ?: ""

    // --- ARAMA FONKSİYONU ---
    fun performSearch(text: String) {
        if (text.length < 2) return

        isLoading = true
        scope.launch {
            try {
                // Supabase'den kullanıcı ara (Kendin hariç)
                searchResults = SupabaseClient.client.from("profiles")
                    .select {
                        filter {
                            ilike("username", "%$text%")
                            neq("username", currentUsername)
                        }
                    }.decodeList<Profile>()
            } catch (e: Exception) {
                // Hata durumunda sessiz kalabilir veya loglayabiliriz
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            // --- GELİŞMİŞ SEARCH BAR ---
            SearchBar(
                query = query,
                onQueryChange = {
                    query = it
                    // Yazdıkça anlık arama yapmak istersen burayı açabilirsin:
                    // performSearch(it)
                },
                onSearch = {
                    performSearch(it)
                    // Klavye enter'a basınca active kalsın ki sonuçları görelim
                },
                active = active,
                onActiveChange = {
                    active = it
                    if (!active) {
                        // Kapatılınca query'i temizlemek istersen:
                        // query = ""
                    }
                },
                placeholder = { Text("Kullanıcı ara...") },
                leadingIcon = {
                    if (active) {
                        // Aktifken Geri Tuşu
                        IconButton(onClick = {
                            active = false
                            query = ""
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                        }
                    } else {
                        // Pasifken Arama İkonu
                        Icon(Icons.Default.Search, contentDescription = "Ara")
                    }
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        // Yazı varsa Temizle butonu
                        IconButton(onClick = {
                            query = ""
                            searchResults = emptyList()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Temizle")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (active) 0.dp else 16.dp) // Aktifken tam ekran, değilken kenarlardan boşluklu
            ) {
                // --- İŞTE BURASI: SEARCHBAR'IN İÇERİĞİ ---
                // Burası artık Scaffold'un body'si yerine geçiyor (Active olduğunda)

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (searchResults.isEmpty()) {
                    // Sonuç yok veya henüz arama yapılmadı
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (query.isEmpty()) {
                            Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Aramaya başlamak için yazın", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            Text("Sonuç bulunamadı: $query", color = MaterialTheme.colorScheme.error)
                        }
                    }
                } else {
                    // SONUÇ LİSTESİ (Burada listeleniyor)
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(searchResults) { profile ->
                            UserListItem(
                                profile = profile,
                                onAddFriend = {
                                    scope.launch {
                                        try {
                                            val newFriendship = Friendship(
                                                sender_username = currentUsername,
                                                receiver_username = profile.username,
                                                status = "pending"
                                            )
                                            SupabaseClient.client.from("friendships").insert(newFriendship)
                                            Toast.makeText(context, "İstek gönderildi!", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Zaten ekli veya hata.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                onClick = {
                                    // Tıklayınca detay açılabilir veya arama kapanabilir
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        // --- SCAFFOLD BODY ---
        // SearchBar "active" değilken burada ne görüneceği.
        // Genelde boş bırakılır veya "Son Eklenenler" gibi bir liste konur.
        // Biz boş bırakıyoruz çünkü her şey SearchBar'ın içinde dönüyor.
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (!active) {
                Text(
                    "Arkadaş bulmak için yukarıya dokun.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// --- YARDIMCI BİLEŞEN: KULLANICI LİSTE ELEMANI ---
@Composable
fun UserListItem(
    profile: Profile,
    onAddFriend: () -> Unit,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(profile.username) },
        supportingContent = {
            if (!profile.full_name.isNullOrEmpty()) {
                Text(profile.full_name)
            }
        },
        leadingContent = {
            // Avatar
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (profile.avatar_url != null) {
                        // Eğer Coil kütüphanesi varsa AsyncImage kullan
                        // AsyncImage(model = profile.avatar_url, ...)
                        // Şimdilik text:
                        Text(
                            text = profile.username.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium
                        )
                    } else {
                        Text(
                            text = profile.username.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        },
        trailingContent = {
            IconButton(onClick = onAddFriend) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = "Arkadaş Ekle",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        modifier = Modifier.clickable { onClick() }
    )
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
}


