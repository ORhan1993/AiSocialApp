package com.bozgeyik.aisocialapp.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.bozgeyik.aisocialapp.data.Friendship
import com.bozgeyik.aisocialapp.data.Post
import com.bozgeyik.aisocialapp.data.Profile
import com.bozgeyik.aisocialapp.data.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // State'ler
    var profile by remember { mutableStateOf<Profile?>(null) }
    var myPosts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var pendingRequests by remember { mutableStateOf<List<Friendship>>(emptyList()) }

    var showEditDialog by remember { mutableStateOf(false) }
    var showRequestsDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    // --- DÜZELTME BURADA YAPILDI ---
    // Sadece emaili değil, kullanıcının gerçek ID'sini de alıyoruz
    val currentUser = SupabaseClient.client.auth.currentUserOrNull()
    val currentUserEmail = currentUser?.email ?: ""
    val currentUserId = currentUser?.id ?: "" // UUID olan ID bu!
    val currentUsername = currentUserEmail.split("@")[0]

    // Profil Fotoğrafı Seçici
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                try {
                    val byteArray = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    }
                    if (byteArray != null) {
                        val fileName = "avatar_${currentUsername}_${UUID.randomUUID()}.jpg"
                        val bucket = SupabaseClient.client.storage.from("images")
                        bucket.upload(fileName, byteArray)
                        val url = bucket.publicUrl(fileName)

                        SupabaseClient.client.from("profiles").update(
                            mapOf("avatar_url" to url)
                        ) { filter { eq("username", currentUsername) } }

                        profile = profile?.copy(avatar_url = url)
                        Toast.makeText(context, "Profil fotoğrafı güncellendi", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    // Hata olursa sessizce geç veya logla
                }
            }
        }
    }

    // Verileri Yükle
    LaunchedEffect(Unit) {
        try {
            val profileList = SupabaseClient.client.from("profiles").select {
                filter { eq("username", currentUsername) }
            }.decodeList<Profile>()

            if (profileList.isNotEmpty()) {
                profile = profileList[0]
            } else {
                // --- KRİTİK DÜZELTME BURADA ---
                // Profil oluştururken boş ID yerine gerçek UUID gönderiyoruz
                if (currentUserId.isNotEmpty()) {
                    val newProfile = Profile(
                        id = currentUserId, // ARTIK BOŞ DEĞİL!
                        username = currentUsername
                    )
                    SupabaseClient.client.from("profiles").insert(newProfile)
                    profile = newProfile
                }
            }

            myPosts = SupabaseClient.client.from("posts").select {
                filter { eq("username", currentUsername) }
                order("id", Order.DESCENDING)
            }.decodeList<Post>()

            pendingRequests = SupabaseClient.client.from("friendships").select {
                filter {
                    eq("receiver_username", currentUsername)
                    eq("status", "pending")
                }
            }.decodeList<Friendship>()

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentUsername, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showRequestsDialog = true }) {
                        BadgedBox(badge = {
                            if (pendingRequests.isNotEmpty()) {
                                Badge { Text("${pendingRequests.size}") }
                            }
                        }) {
                            Icon(Icons.Default.Notifications, contentDescription = "İstekler")
                        }
                    }

                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Ayarlar")
                    }

                    IconButton(onClick = {
                        scope.launch {
                            SupabaseClient.client.auth.signOut()
                            onLogout()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Çıkış", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {

                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            .clickable { imagePicker.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (profile?.avatar_url != null) {
                            AsyncImage(
                                model = profile!!.avatar_url,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(50.dp), tint = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ProfileStat(count = myPosts.size.toString(), label = "Gönderi")
                        ProfileStat(count = "1.2K", label = "Takipçi")
                        ProfileStat(count = "340", label = "Takip")
                    }
                }

                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = profile?.full_name ?: currentUsername,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    if (!profile?.bio.isNullOrEmpty()) {
                        Text(text = profile!!.bio!!, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Button(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface)
                    ) {
                        Text("Profili Düzenle")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Icon(Icons.Default.GridOn, contentDescription = "Grid", tint = MaterialTheme.colorScheme.primary)
                    Icon(Icons.Default.Lock, contentDescription = "Private", tint = Color.Gray)
                }
                HorizontalDivider(modifier = Modifier.padding(top = 8.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(2.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(myPosts) { post ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .background(Color.LightGray)
                        ) {
                            if (post.image_url != null) {
                                AsyncImage(
                                    model = post.image_url,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        var newName by remember { mutableStateOf(profile?.full_name ?: "") }
        var newBio by remember { mutableStateOf(profile?.bio ?: "") }
        var isPrivate by remember { mutableStateOf(profile?.is_private ?: false) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Profili Düzenle") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Ad Soyad") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newBio,
                        onValueChange = { newBio = it },
                        label = { Text("Biyografi") }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(checked = isPrivate, onCheckedChange = { isPrivate = it })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if(isPrivate) "Gizli Hesap" else "Herkese Açık")
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        SupabaseClient.client.from("profiles").update(
                            mapOf(
                                "full_name" to newName,
                                "bio" to newBio,
                                "is_private" to isPrivate
                            )
                        ) { filter { eq("username", currentUsername) } }

                        profile = profile?.copy(full_name = newName, bio = newBio, is_private = isPrivate)
                        showEditDialog = false
                    }
                }) { Text("Kaydet") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("İptal") }
            }
        )
    }

    if (showRequestsDialog) {
        AlertDialog(
            onDismissRequest = { showRequestsDialog = false },
            title = { Text("Arkadaşlık İstekleri") },
            text = {
                if (pendingRequests.isEmpty()) {
                    Text("Hiç yeni istek yok.")
                } else {
                    Column {
                        pendingRequests.forEach { request ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(request.sender_username, fontWeight = FontWeight.Bold)
                                Row {
                                    IconButton(onClick = {
                                        scope.launch {
                                            SupabaseClient.client.from("friendships").update(
                                                mapOf("status" to "accepted")
                                            ) { filter { eq("id", request.id) } }

                                            pendingRequests = pendingRequests.filter { it.id != request.id }
                                            Toast.makeText(context, "Kabul Edildi", Toast.LENGTH_SHORT).show()
                                        }
                                    }) { Icon(Icons.Default.Check, null, tint = Color.Green) }

                                    IconButton(onClick = {
                                        scope.launch {
                                            SupabaseClient.client.from("friendships").update(
                                                mapOf("status" to "rejected")
                                            ) { filter { eq("id", request.id) } }

                                            pendingRequests = pendingRequests.filter { it.id != request.id }
                                        }
                                    }) { Icon(Icons.Default.Close, null, tint = Color.Red) }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRequestsDialog = false }) { Text("Kapat") }
            }
        )
    }
}

@Composable
fun ProfileStat(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
    }
}