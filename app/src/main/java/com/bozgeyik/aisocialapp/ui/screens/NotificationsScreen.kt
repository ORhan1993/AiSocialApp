package com.bozgeyik.aisocialapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bozgeyik.aisocialapp.data.Notification
import com.bozgeyik.aisocialapp.data.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(navController: NavController) {
    var notifications by remember { mutableStateOf<List<Notification>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val pullRefreshState = rememberPullToRefreshState()
    val currentUser = SupabaseClient.client.auth.currentUserOrNull()
    val currentUserId = currentUser?.id ?: ""

    suspend fun loadNotifications() {
        if (currentUserId.isNotEmpty()) {
            try {
                notifications = SupabaseClient.client.from("notifications")
                    .select {
                        filter { eq("user_id", currentUserId) }
                        order("created_at", Order.DESCENDING)
                    }.decodeList<Notification>()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        isLoading = false
    }

    LaunchedEffect(Unit) { loadNotifications() }

    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            loadNotifications()
            delay(500)
            pullRefreshState.endRefresh()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bildirimler") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().nestedScroll(pullRefreshState.nestedScrollConnection)) {
            if (isLoading && !pullRefreshState.isRefreshing) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (notifications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Notifications, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Henüz bildirim yok.", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(notifications) { notif ->
                        NotificationItem(notif)
                    }
                }
            }
            PullToRefreshContainer(state = pullRefreshState, modifier = Modifier.align(Alignment.TopCenter))
        }
    }
}

@Composable
fun NotificationItem(notification: Notification) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape)
                .background(if (notification.type == "like") Color.Red.copy(alpha = 0.1f) else Color.Blue.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (notification.type == "like") Icons.Default.Favorite else Icons.Default.Comment,
                contentDescription = null,
                tint = if (notification.type == "like") Color.Red else Color.Blue,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Row {
                Text(text = notification.actor_username, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = notification.message)
            }
            Text(text = notification.created_at.take(10), fontSize = 12.sp, color = Color.Gray)
        }
    }
    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
}