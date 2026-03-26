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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.outlined.ModeComment
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import com.bozgeyik.aisocialapp.data.Friendship
import com.bozgeyik.aisocialapp.data.Like
import com.bozgeyik.aisocialapp.data.Post
import com.bozgeyik.aisocialapp.data.Story
import com.bozgeyik.aisocialapp.data.SupabaseClient
import com.bozgeyik.aisocialapp.ui.components.VideoPlayer
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddPost: () -> Unit,
    onNavigateToAddStory: () -> Unit,
    onNavigateToMessages: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var stories by remember { mutableStateOf<List<Story>>(emptyList()) }
    var likedPostIds by remember { mutableStateOf<List<Long>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var selectedTab by remember { mutableStateOf(0) }
    val pullRefreshState = rememberPullToRefreshState()
    var showCommentSheet by remember { mutableStateOf(false) }
    var selectedPostId by remember { mutableStateOf<Long?>(null) }
    var currentStory by remember { mutableStateOf<Story?>(null) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val currentUser = SupabaseClient.client.auth.currentUserOrNull()
    val currentUserEmail = currentUser?.email ?: ""
    val currentUserId = currentUser?.id ?: ""
    val currentUsername = currentUserEmail.split("@")[0]

    suspend fun fetchData() {
        try {
            stories = SupabaseClient.client.from("stories")
                .select { order("id", Order.DESCENDING) }
                .decodeList<Story>()

            if (currentUserId.isNotEmpty()) {
                val myLikes = SupabaseClient.client.from("likes")
                    .select { filter { eq("user_id", currentUserId) } }
                    .decodeList<Like>()
                likedPostIds = myLikes.map { it.post_id }
            }

            if (selectedTab == 0) {
                posts = SupabaseClient.client.from("posts")
                    .select { order("id", Order.DESCENDING) }
                    .decodeList<Post>()
            } else {
                val friendships = SupabaseClient.client.from("friendships").select {
                    filter {
                        or {
                            eq("sender_username", currentUsername)
                            eq("receiver_username", currentUsername)
                        }
                        eq("status", "accepted")
                    }
                }.decodeList<Friendship>()

                val friendUsernames = friendships.map {
                    if (it.sender_username == currentUsername) it.receiver_username else it.sender_username
                }.toMutableList()

                friendUsernames.add(currentUsername)

                if (friendUsernames.isNotEmpty()) {
                    posts = SupabaseClient.client.from("posts").select {
                        filter { isIn("username", friendUsernames) }
                        order("id", Order.DESCENDING)
                    }.decodeList<Post>()
                } else {
                    posts = emptyList()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(selectedTab) {
        isLoading = true
        fetchData()
    }

    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            fetchData()
            delay(500)
            pullRefreshState.endRefresh()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                    title = {
                        Text(
                            "Ai Social",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    actions = {
                        IconButton(onClick = onNavigateToNotifications) {
                            Icon(Icons.Default.Notifications, contentDescription = "Bildirimler", tint = MaterialTheme.colorScheme.onBackground)
                        }
                        IconButton(onClick = onNavigateToMessages) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Mesajlar",
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.rotate(-45f).padding(bottom = 4.dp)
                            )
                        }
                    }
                )

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Public, null, Modifier.size(16.dp), tint = if(selectedTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f))
                                Spacer(Modifier.width(8.dp))
                                Text("Keşfet", color = if(selectedTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f))
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.People, null, Modifier.size(16.dp), tint = if(selectedTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f))
                                Spacer(Modifier.width(8.dp))
                                Text("Arkadaşlar", color = if(selectedTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha=0.6f))
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .nestedScroll(pullRefreshState.nestedScrollConnection)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    StoriesSection(
                        stories = stories,
                        onAddStoryClick = onNavigateToAddStory,
                        onStoryClick = { clickedStory -> currentStory = clickedStory }
                    )
                }

                if (!isLoading && posts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (selectedTab == 1) "Arkadaşların henüz paylaşım yapmadı." else "Henüz gönderi yok.",
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                )
                                if (selectedTab == 1) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Keşfet sekmesinden yeni arkadaşlar bul!",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(posts) { post ->
                        val isLikedByMe = likedPostIds.contains(post.id)
                        PostCard(
                            post = post,
                            isLikedInitially = isLikedByMe,
                            currentUsername = currentUsername,
                            onLikeClick = {
                                if (currentUserId.isNotEmpty()) {
                                    scope.launch {
                                        if (isLikedByMe) {
                                            val newCount = (post.like_count - 1).coerceAtLeast(0)
                                            posts = posts.map { if (it.id == post.id) it.copy(like_count = newCount) else it }
                                            likedPostIds = likedPostIds - post.id
                                            SupabaseClient.client.from("likes").delete {
                                                filter { eq("user_id", currentUserId); eq("post_id", post.id) }
                                            }
                                            SupabaseClient.client.from("posts").update(mapOf("like_count" to newCount)) {
                                                filter { eq("id", post.id) }
                                            }
                                        } else {
                                            val newCount = post.like_count + 1
                                            posts = posts.map { if (it.id == post.id) it.copy(like_count = newCount) else it }
                                            likedPostIds = likedPostIds + post.id
                                            SupabaseClient.client.from("likes").insert(Like(user_id = currentUserId, post_id = post.id))
                                            SupabaseClient.client.from("posts").update(mapOf("like_count" to newCount)) {
                                                filter { eq("id", post.id) }
                                            }
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "Beğenmek için giriş yapmalısınız", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onCommentClick = {
                                selectedPostId = post.id
                                showCommentSheet = true
                            },
                            onShareClick = {
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "@${post.username}: ${post.description}\n${post.image_url}")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Paylaş"))
                            },
                            onDeleteClick = {
                                scope.launch {
                                    posts = posts.filter { it.id != post.id }
                                    SupabaseClient.client.from("posts").delete { filter { eq("id", post.id) } }
                                    Toast.makeText(context, "Gönderi silindi", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }

            if (isLoading && !pullRefreshState.isRefreshing) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary)
            }

            PullToRefreshContainer(
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            )
        }

        if (showCommentSheet && selectedPostId != null) {
            ModalBottomSheet(
                onDismissRequest = { showCommentSheet = false },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                CommentsSection(postId = selectedPostId!!)
            }
        }

        if (currentStory != null) {
            StoryViewer(story = currentStory!!, onDismiss = { currentStory = null })
        }
    }
}

@Composable
fun PostCard(
    post: Post,
    isLikedInitially: Boolean,
    currentUsername: String,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isMyPost = post.username == currentUsername

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp).border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.large)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (post.image_url != null) {
                if (post.media_type == "video") {
                    Box(modifier = Modifier.fillMaxWidth().height(400.dp).background(Color.Black)) {
                        VideoPlayer(videoUrl = post.image_url)
                    }
                } else {
                    AsyncImage(
                        model = post.image_url,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(400.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                shape = CircleShape,
                modifier = Modifier.padding(12.dp).align(Alignment.TopStart)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(post.username.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("@${post.username}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            if (isMyPost) {
                Surface(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                    shape = CircleShape,
                    modifier = Modifier.padding(12.dp).align(Alignment.TopEnd).clickable { onDeleteClick() }
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Sil", tint = Color.White, modifier = Modifier.padding(8.dp).size(20.dp))
                }
            }
        }

        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onLikeClick) {
                    Icon(
                        imageVector = if (isLikedInitially) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Beğen",
                        tint = if (isLikedInitially) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(28.dp)
                    )
                }
                IconButton(onClick = onCommentClick) {
                    Icon(Icons.Outlined.ModeComment, contentDescription = "Yorum", modifier = Modifier.size(26.dp), tint = MaterialTheme.colorScheme.onSurface)
                }
                IconButton(onClick = onShareClick) {
                    Icon(Icons.Outlined.Share, contentDescription = "Paylaş", modifier = Modifier.size(26.dp), tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            Text(
                text = "${post.like_count} beğenme",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            val descriptionText = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)) {
                    append(post.username)
                }
                append("  ")
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                    append(post.description)
                }
            }
            Text(text = descriptionText, fontSize = 14.sp, modifier = Modifier.padding(start = 8.dp))

            Text(
                text = "Yorumları gör...",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp, start = 8.dp).clickable { onCommentClick() }
            )
        }
    }
}

@Composable
fun StoriesSection(
    stories: List<Story>,
    onAddStoryClick: () -> Unit,
    onStoryClick: (Story) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(70.dp).border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape).padding(4.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant).clickable { onAddStoryClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Hikaye Ekle", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Hikayen", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground)
            }
        }

        items(stories) { story ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(70.dp).border(
                        width = 2.5.dp,
                        brush = Brush.linearGradient(colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)),
                        shape = CircleShape
                    ).padding(5.dp).clip(CircleShape).clickable { onStoryClick(story) }
                ) {
                    AsyncImage(model = story.image_url, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(story.username, fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground)
            }
        }
    }
}

@Composable
fun CommentsSection(postId: Long) {
    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var newCommentText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val currentUser = SupabaseClient.client.auth.currentUserOrNull()
    val currentUserId = currentUser?.id ?: ""

    LaunchedEffect(postId) {
        comments = SupabaseClient.client.from("comments")
            .select { filter { eq("post_id", postId) }; order("created_at", Order.DESCENDING) }.decodeList<Comment>()
    }

    Column(
        modifier = Modifier.fillMaxWidth().height(500.dp).padding(16.dp)
    ) {
        Box(
            modifier = Modifier.width(40.dp).height(4.dp).background(MaterialTheme.colorScheme.outlineVariant, CircleShape).align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("Yorumlar", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

        LazyColumn(modifier = Modifier.weight(1f).padding(top = 8.dp)) {
            if (comments.isEmpty()) {
                item { Text("Henüz yorum yok. İlk sen yaz!", color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.5f)) }
            }
            items(comments) { comment ->
                Row(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(text = comment.username, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = comment.content, color = MaterialTheme.colorScheme.onSurface)
                }
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
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
                shape = CircleShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
            IconButton(onClick = {
                if (newCommentText.isNotEmpty() && currentUserId.isNotEmpty()) {
                    scope.launch {
                        val username = currentUser?.email?.split("@")?.get(0) ?: "Anonim"
                        val newComment = Comment(post_id = postId, username = username, content = newCommentText)
                        SupabaseClient.client.from("comments").insert(newComment)
                        comments = listOf(newComment) + comments
                        newCommentText = ""
                        Toast.makeText(context, "Yorum gönderildi", Toast.LENGTH_SHORT).show()
                    }
                } else if(currentUserId.isEmpty()) {
                    Toast.makeText(context, "Giriş yapmalısınız", Toast.LENGTH_SHORT).show()
                }
            }) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Gönder", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun StoryViewer(story: Story, onDismiss: () -> Unit) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(story) {
        progress.animateTo(targetValue = 1f, animationSpec = tween(durationMillis = 5000, easing = LinearEasing))
        onDismiss()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black).clickable { onDismiss() }) {
            AsyncImage(model = story.image_url, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)

            Column(modifier = Modifier.fillMaxWidth().padding(top = 40.dp, start = 16.dp, end = 16.dp)) {
                LinearProgressIndicator(
                    progress = { progress.value },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f),
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.DarkGray), contentAlignment = Alignment.Center) {
                        Text(story.username.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(story.username, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}