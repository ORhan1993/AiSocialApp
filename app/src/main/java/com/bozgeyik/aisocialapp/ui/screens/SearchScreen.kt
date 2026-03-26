package com.bozgeyik.aisocialapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.bozgeyik.aisocialapp.data.Friendship
import com.bozgeyik.aisocialapp.data.Profile
import com.bozgeyik.aisocialapp.data.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavController) {
    var query by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<Profile>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val currentUser = SupabaseClient.client.auth.currentUserOrNull()
    val currentUsername = currentUser?.email?.split("@")?.get(0) ?: ""

    fun performSearch(text: String) {
        if (text.length < 2) return
        isLoading = true
        scope.launch {
            try {
                searchResults = SupabaseClient.client.from("profiles")
                    .select {
                        filter { ilike("username", "%$text%"); neq("username", currentUsername) }
                    }.decodeList<Profile>()
            } catch (e: Exception) {
            } finally { isLoading = false }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            SearchBar(
                query = query,
                onQueryChange = { query = it },
                onSearch = { performSearch(it) },
                active = active,
                onActiveChange = { active = it },
                placeholder = { Text("Kullanıcı ara...") },
                colors = SearchBarDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
                leadingIcon = {
                    if (active) {
                        IconButton(onClick = { active = false; query = "" }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    } else {
                        Icon(Icons.Default.Search, contentDescription = "Ara", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = ""; searchResults = emptyList() }) {
                            Icon(Icons.Default.Close, contentDescription = "Temizle", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(if (active) 0.dp else 16.dp)
            ) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (searchResults.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
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
                                            val newFriendship = Friendship(sender_username = currentUsername, receiver_username = profile.username, status = "pending")
                                            SupabaseClient.client.from("friendships").insert(newFriendship)
                                            Toast.makeText(context, "İstek gönderildi!", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Zaten ekli veya hata.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                onClick = { }
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (!active) {
                Text(
                    "Arkadaş bulmak için yukarıya dokun.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f)
                )
            }
        }
    }
}

@Composable
fun UserListItem(profile: Profile, onAddFriend: () -> Unit, onClick: () -> Unit) {
    ListItem(
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
        headlineContent = { Text(profile.username, color = MaterialTheme.colorScheme.onBackground) },
        supportingContent = {
            if (!profile.full_name.isNullOrEmpty()) {
                Text(profile.full_name, color = MaterialTheme.colorScheme.onBackground.copy(alpha=0.7f))
            }
        },
        leadingContent = {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = profile.username.take(1).uppercase(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        trailingContent = {
            IconButton(onClick = onAddFriend) {
                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Arkadaş Ekle", tint = MaterialTheme.colorScheme.primary)
            }
        },
        modifier = Modifier.clickable { onClick() }
    )
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}