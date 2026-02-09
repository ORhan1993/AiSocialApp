package com.bozgeyik.aisocialapp.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.ModeComment
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.bozgeyik.aisocialapp.data.Comment
import com.bozgeyik.aisocialapp.data.Post
import com.bozgeyik.aisocialapp.data.Story
import com.bozgeyik.aisocialapp.data.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddPost: () -> Unit,
    onNavigateToAddStory: () -> Unit,
    onNavigateToMessages: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    // --- STATE TANIMLAMALARI ---
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var stories by remember { mutableStateOf<List<Story>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Yorum Penceresi Kontrolü
    var showCommentSheet by remember { mutableStateOf(false) }
    var selectedPostId by remember { mutableStateOf<Long?>(null) }

    // HİKAYE İZLEME KONTROLÜ (YENİ EKLENDİ)
    var currentStory by remember { mutableStateOf<Story?>(null) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // --- VERİ ÇEKME İŞLEMİ ---
    LaunchedEffect(Unit) {
        try {
            val postsList = SupabaseClient.client.from("posts")
                .select { order("id", Order.DESCENDING) }
                .decodeList<Post>()
            posts = postsList

            val storyList = SupabaseClient.client.from("stories")
                .select { order("id", Order.DESCENDING) }
                .decodeList<Story>()
            stories = storyList

        } catch (e: Exception) {
            // Hata yönetimi
        } finally {
            isLoading = false
        }
    }

    // --- ANA EKRAN YAPISI ---
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Ai Social",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToMessages) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Mesajlar",
                            modifier = Modifier
                                .rotate(-45f)
                                .padding(bottom = 4.dp)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // 1. BÖLÜM: HİKAYELER ÇUBUĞU
                item {
                    StoriesSection(
                        stories = stories,
                        onAddStoryClick = onNavigateToAddStory,
                        // Tıklanan hikayeyi state'e atıyoruz
                        onStoryClick = { clickedStory ->
                            currentStory = clickedStory
                        }
                    )
                }

                // 2. BÖLÜM: GÖNDERİ LİSTESİ
                if (!isLoading && posts.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            Text("Henüz gönderi yok.", color = Color.Gray)
                        }
                    }
                } else {
                    items(posts) { post ->
                        PostCard(
                            post = post,
                            onLikeClick = {
                                scope.launch {
                                    val newCount = post.like_count + 1
                                    posts = posts.map { if (it.id == post.id) it.copy(like_count = newCount) else it }
                                    SupabaseClient.client.from("posts").update(
                                        mapOf("like_count" to newCount)
                                    ) { filter { eq("id", post.id) } }
                                }
                            },
                            onCommentClick = {
                                selectedPostId = post.id
                                showCommentSheet = true
                            },
                            onShareClick = {
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "@${post.username} bir gönderi paylaştı:\n${post.description}\n\n${post.image_url}")
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Paylaş")
                                context.startActivity(shareIntent)
                            }
                        )
                    }
                }
            }

            // Yükleniyor Göstergesi
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }

        // --- YORUM PENCERESİ (BOTTOM SHEET) ---
        if (showCommentSheet && selectedPostId != null) {
            ModalBottomSheet(
                onDismissRequest = { showCommentSheet = false }
            ) {
                CommentsSection(postId = selectedPostId!!)
            }
        }

        // --- HİKAYE İZLEYİCİ (FULL SCREEN) ---
        // Eğer bir hikaye seçiliyse bunu göster
        if (currentStory != null) {
            StoryViewer(
                story = currentStory!!,
                onDismiss = { currentStory = null } // Kapatınca null yap
            )
        }
    }
}

// --- BİLEŞEN 1: HİKAYELER ---
@Composable
fun StoriesSection(
    stories: List<Story>,
    onAddStoryClick: () -> Unit,
    onStoryClick: (Story) -> Unit // YENİ PARAMETRE
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // "Hikaye Ekle" Butonu
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .border(1.dp, Color.Gray, CircleShape)
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onAddStoryClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Hikaye Ekle",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Hikayen", fontSize = 12.sp)
            }
        }

        // Diğer Hikayeler Listesi
        items(stories) { story ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .border(
                            width = 2.5.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFF58529), Color(0xFFDD2A7B), Color(0xFF8134AF))
                            ),
                            shape = CircleShape
                        )
                        .padding(5.dp)
                        .clip(CircleShape)
                        // TIKLAMA BURADA
                        .clickable { onStoryClick(story) }
                ) {
                    AsyncImage(
                        model = story.image_url,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(story.username, fontSize = 12.sp)
            }
        }
    }
}

// --- BİLEŞEN 2: MODERN GÖNDERİ KARTI ---
@Composable
fun PostCard(
    post: Post,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit
) {
    var isLiked by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (post.image_url != null) {
                AsyncImage(
                    model = post.image_url,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                shape = CircleShape,
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.TopStart)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(post.username.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("@${post.username}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            if (post.is_ai_generated) {
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Text(
                        text = "AI Generated",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    isLiked = !isLiked
                    onLikeClick()
                }) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Beğen",
                        tint = if (isLiked) Color.Red else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(28.dp)
                    )
                }
                IconButton(onClick = onCommentClick) {
                    Icon(Icons.Outlined.ModeComment, contentDescription = "Yorum", modifier = Modifier.size(26.dp))
                }
                IconButton(onClick = onShareClick) {
                    Icon(Icons.Outlined.Share, contentDescription = "Paylaş", modifier = Modifier.size(26.dp))
                }
            }

            Text(
                text = "${post.like_count} beğenme",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            val descriptionText = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(post.username)
                }
                append("  ")
                append(post.description)
            }
            Text(text = descriptionText, fontSize = 14.sp, lineHeight = 18.sp, modifier = Modifier.padding(start = 8.dp))

            Text(
                text = "Yorumları gör...",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(top = 4.dp, start = 8.dp)
                    .clickable { onCommentClick() }
            )
        }
    }
}

// --- BİLEŞEN 3: YORUMLAR ---
@Composable
fun CommentsSection(postId: Long) {
    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var newCommentText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(postId) {
        comments = SupabaseClient.client.from("comments")
            .select {
                filter { eq("post_id", postId) }
                order("created_at", Order.DESCENDING)
            }.decodeList<Comment>()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp)
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .background(Color.LightGray, CircleShape)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("Yorumlar", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        LazyColumn(modifier = Modifier.weight(1f).padding(top = 8.dp)) {
            if (comments.isEmpty()) {
                item { Text("Henüz yorum yok. İlk sen yaz!", color = Color.Gray) }
            }
            items(comments) { comment ->
                Row(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(text = comment.username, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = comment.content)
                }
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            OutlinedTextField(
                value = newCommentText,
                onValueChange = { newCommentText = it },
                placeholder = { Text("Yorum ekle...") },
                modifier = Modifier.weight(1f),
                shape = CircleShape
            )
            IconButton(onClick = {
                if (newCommentText.isNotEmpty()) {
                    scope.launch {
                        val userEmail = SupabaseClient.client.auth.currentUserOrNull()?.email ?: "Anonim"
                        val username = userEmail.split("@")[0]
                        val newComment = Comment(post_id = postId, username = username, content = newCommentText)
                        SupabaseClient.client.from("comments").insert(newComment)
                        comments = listOf(newComment) + comments
                        newCommentText = ""
                        Toast.makeText(context, "Yorum gönderildi", Toast.LENGTH_SHORT).show()
                    }
                }
            }) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Gönder", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// --- BİLEŞEN 4: HİKAYE İZLEYİCİ (ANIMASYONLU) ---
@Composable
fun StoryViewer(story: Story, onDismiss: () -> Unit) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(story) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 5000, easing = LinearEasing)
        )
        onDismiss()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { onDismiss() }
        ) {
            AsyncImage(
                model = story.image_url,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 16.dp, end = 16.dp)
            ) {
                LinearProgressIndicator(
                    progress = { progress.value },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = Color.White,
                    trackColor = Color.Gray.copy(alpha = 0.5f),
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.Gray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(story.username.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(story.username, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}