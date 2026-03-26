package com.bozgeyik.aisocialapp.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var profile by remember { mutableStateOf<Profile?>(null) }
    var myPosts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var pendingRequests by remember { mutableStateOf<List<Friendship>>(emptyList()) }

    var showEditDialog by remember { mutableStateOf(false) }
    var showRequestsDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    val pullRefreshState = rememberPullToRefreshState()

    val currentUser = SupabaseClient.client.auth.currentUserOrNull()
    val currentUserEmail = currentUser?.email ?: ""
    val currentUserId = currentUser?.id ?: ""
    val currentUsername = currentUserEmail.split("@")[0]

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                try {
                    val byteArray = withContext(Dispatchers.IO) { context.contentResolver.openInputStream(uri)?.use { it.readBytes() } }
                    if (byteArray != null) {
                        val fileName = "avatar_${currentUsername}_${UUID.randomUUID()}.jpg"
                        val bucket = SupabaseClient.client.storage.from("images")
                        bucket.upload(fileName, byteArray)
                        val url = bucket.publicUrl(fileName)
                        SupabaseClient.client.from("profiles").update(mapOf("avatar_url" to url)) { filter { eq("username", currentUsername) } }
                        profile = profile?.copy(avatar_url = url)
                        Toast.makeText(context, "Profil fotoğrafı güncellendi", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) { Toast.makeText(context, "Hata oluştu", Toast.LENGTH_SHORT).show() }
            }
        }
    }

    suspend fun loadProfileData() {
        isLoading = true
        try {
            val profileList = SupabaseClient.client.from("profiles").select { filter { eq("username", currentUsername) } }.decodeList<Profile>()
            if (profileList.isNotEmpty()) {
                profile = profileList[0]
            } else if (currentUserId.isNotEmpty()) {
                val newProfile = Profile(id = currentUserId, username = currentUsername)
                SupabaseClient.client.from("profiles").insert(newProfile)
                profile = newProfile
            }

            myPosts = SupabaseClient.client.from("posts").select { filter { eq("username", currentUsername) }; order("id", Order.DESCENDING) }.decodeList<Post>()
            pendingRequests = SupabaseClient.client.from("friendships").select { filter { eq("receiver_username", currentUsername); eq("status", "pending") } }.decodeList<Friendship>()
        } catch (e: Exception) { e.printStackTrace() } finally { isLoading = false }
    }

    LaunchedEffect(Unit) { loadProfileData() }

    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(true) { loadProfileData(); delay(500); pullRefreshState.endRefresh() }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                title = { Text(currentUsername, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
                actions = {
                    IconButton(onClick = { showRequestsDialog = true }) {
                        BadgedBox(badge = { if (pendingRequests.isNotEmpty()) Badge { Text("${pendingRequests.size}") } }) {
                            Icon(Icons.Default.Notifications, contentDescription = "İstekler", tint = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Ayarlar", tint = MaterialTheme.colorScheme.onBackground)
                    }
                    IconButton(onClick = { scope.launch { SupabaseClient.client.auth.signOut(); onLogout() } }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Çıkış", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().nestedScroll(pullRefreshState.nestedScrollConnection)) {
            if (isLoading && !pullRefreshState.isRefreshing) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary)
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(90.dp).clip(CircleShape).border(2.dp, MaterialTheme.colorScheme.primary, CircleShape).clickable { imagePicker.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (profile?.avatar_url != null) {
                                AsyncImage(model = profile!!.avatar_url, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            } else {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(50.dp), tint = MaterialTheme.colorScheme.onBackground.copy(alpha=0.5f))
                            }
                        }
                        Spacer(modifier = Modifier.width(20.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            ProfileStat(count = myPosts.size.toString(), label = "Gönderi")
                            ProfileStat(count = "0", label = "Takipçi")
                            ProfileStat(count = "0", label = "Takip")
                        }
                    }

                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(text = profile?.full_name ?: currentUsername, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                        if (!profile?.bio.isNullOrEmpty()) {
                            Text(text = profile!!.bio!!, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Button(
                            onClick = { showEditDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                        ) { Text("Profili Düzenle", fontWeight = FontWeight.SemiBold) }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        Icon(Icons.Default.GridOn, contentDescription = "Grid", tint = MaterialTheme.colorScheme.primary)
                        Icon(Icons.Default.Lock, contentDescription = "Private", tint = MaterialTheme.colorScheme.onBackground.copy(alpha=0.3f))
                    }
                    HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(2.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(myPosts) { post ->
                            Box(
                                modifier = Modifier.aspectRatio(1f).padding(1.dp).background(MaterialTheme.colorScheme.surfaceVariant)
                                    .combinedClickable(
                                        onClick = { },
                                        onLongClick = {
                                            scope.launch {
                                                SupabaseClient.client.from("posts").delete { filter { eq("id", post.id) } }
                                                myPosts = myPosts.filter { it.id != post.id }
                                                Toast.makeText(context, "Silindi!", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                            ) {
                                if (post.image_url != null) {
                                    if (post.media_type == "video") {
                                        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Video", tint = Color.White)
                                        }
                                    } else {
                                        AsyncImage(model = post.image_url, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                    }
                                }
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(16.dp))
                            }
                        }
                    }
                }
            }
            PullToRefreshContainer(state = pullRefreshState, modifier = Modifier.align(Alignment.TopCenter), containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.primary)
        }
    }

    if (showEditDialog) {
        var newName by remember { mutableStateOf(profile?.full_name ?: "") }
        var newBio by remember { mutableStateOf(profile?.bio ?: "") }
        var isPrivate by remember { mutableStateOf(profile?.is_private ?: false) }

        AlertDialog(
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { showEditDialog = false },
            title = { Text("Profili Düzenle", color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column {
                    OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Ad Soyad") })
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = newBio, onValueChange = { newBio = it }, label = { Text("Biyografi") })
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(checked = isPrivate, onCheckedChange = { isPrivate = it })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if(isPrivate) "Gizli Hesap" else "Herkese Açık", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            },
            confirmButton = { Button(onClick = {
                scope.launch {
                    SupabaseClient.client.from("profiles").update(mapOf("full_name" to newName, "bio" to newBio, "is_private" to isPrivate)) { filter { eq("username", currentUsername) } }
                    profile = profile?.copy(full_name = newName, bio = newBio, is_private = isPrivate)
                    showEditDialog = false
                }
            }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Text("Kaydet") } },
            dismissButton = { TextButton(onClick = { showEditDialog = false }) { Text("İptal", color = MaterialTheme.colorScheme.onSurface) } }
        )
    }

    if (showRequestsDialog) {
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { showRequestsDialog = false },
            title = { Text("Arkadaşlık İstekleri", color = MaterialTheme.colorScheme.onSurface) },
            text = {
                if (pendingRequests.isEmpty()) { Text("Hiç yeni istek yok.", color = MaterialTheme.colorScheme.onSurface) }
                else {
                    Column {
                        pendingRequests.forEach { request ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(request.sender_username, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Row {
                                    IconButton(onClick = {
                                        scope.launch {
                                            SupabaseClient.client.from("friendships").update(mapOf("status" to "accepted")) { filter { eq("id", request.id) } }
                                            pendingRequests = pendingRequests.filter { it.id != request.id }
                                            Toast.makeText(context, "Kabul Edildi", Toast.LENGTH_SHORT).show()
                                        }
                                    }) { Icon(Icons.Default.Check, null, tint = Color(0xFF4CAF50)) }

                                    IconButton(onClick = {
                                        scope.launch {
                                            SupabaseClient.client.from("friendships").update(mapOf("status" to "rejected")) { filter { eq("id", request.id) } }
                                            pendingRequests = pendingRequests.filter { it.id != request.id }
                                        }
                                    }) { Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.error) }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showRequestsDialog = false }) { Text("Kapat", color = MaterialTheme.colorScheme.primary) } }
        )
    }
}

@Composable
fun ProfileStat(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f))
    }
}