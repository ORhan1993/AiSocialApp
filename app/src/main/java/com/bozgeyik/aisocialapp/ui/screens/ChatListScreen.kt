package com.bozgeyik.aisocialapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bozgeyik.aisocialapp.data.Message
import com.bozgeyik.aisocialapp.data.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(navController: NavController) {
    var chatList by remember { mutableStateOf<List<ChatSummary>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val currentUserEmail = SupabaseClient.client.auth.currentUserOrNull()?.email ?: ""
    val currentUsername = currentUserEmail.split("@")[0]

    // Verileri Çek ve İşle
    LaunchedEffect(Unit) {
        try {
            // 1. Bana gelen veya benim attığım TÜM mesajları çek
            val allMessages = SupabaseClient.client.from("messages").select {
                filter {
                    or {
                        eq("sender_username", currentUsername)
                        eq("receiver_username", currentUsername)
                    }
                }
                order("created_at", Order.DESCENDING) // En yeniler önce
            }.decodeList<Message>()

            // 2. Mesajları Kişilere Göre Grupla
            val grouped = allMessages.groupBy { msg ->
                if (msg.sender_username == currentUsername) msg.receiver_username else msg.sender_username
            }

            // 3. Her grubun en son mesajını al ve listeye çevir
            val summaries = grouped.map { (username, messages) ->
                val lastMsg = messages.first() // Zaten sıralı geldiği için ilki en yenisidir
                ChatSummary(
                    username = username,
                    lastMessage = lastMsg.content ?: "Resim/Dosya",
                    time = lastMsg.created_at?.take(10) ?: "" // Sadece tarihi al (YYYY-MM-DD)
                )
            }

            chatList = summaries

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Sohbetler", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            if (chatList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Henüz mesajın yok.", color = Color.Gray)
                        Button(onClick = { navController.navigate("search") }, modifier = Modifier.padding(top = 16.dp)) {
                            Text("Yeni Sohbet Başlat")
                        }
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.padding(padding)) {
                    items(chatList) { chat ->
                        ChatListItem(chat) {
                            // Tıklayınca o kişiyle sohbete git
                            navController.navigate("chat/${chat.username}")
                        }
                    }
                }
            }
        }
    }
}

// Yardımcı Veri Sınıfı (Sadece bu ekran için)
data class ChatSummary(
    val username: String,
    val lastMessage: String,
    val time: String
)

@Composable
fun ChatListItem(chat: ChatSummary, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profil Resmi (Basit Harf)
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = chat.username.take(1).uppercase(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // İsim ve Son Mesaj
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = chat.username, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = chat.time, fontSize = 12.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = chat.lastMessage,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
}