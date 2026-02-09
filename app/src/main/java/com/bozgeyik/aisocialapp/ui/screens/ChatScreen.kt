package com.bozgeyik.aisocialapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bozgeyik.aisocialapp.data.Message
import com.bozgeyik.aisocialapp.data.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(receiverUsername: String, sharedPostId: Long? = null) {
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var newMessage by remember { mutableStateOf("") }
    val currentUserEmail = SupabaseClient.client.auth.currentUserOrNull()?.email ?: ""
    val currentUsername = currentUserEmail.split("@")[0]
    val scope = rememberCoroutineScope()

    // Mesajları Sürekli Getir (Basit Realtime)
    LaunchedEffect(Unit) {
        // Eğer paylaşılmış bir post ile geldiyse, otomatik mesaj gönder
        if (sharedPostId != null && sharedPostId != 0L) {
            val shareMsg = Message(
                sender_username = currentUsername,
                receiver_username = receiverUsername,
                content = "Bir gönderi paylaştı",
                post_id = sharedPostId
            )
            SupabaseClient.client.from("messages").insert(shareMsg)
        }

        // Sohbeti Dinle
        while (true) {
            try {
                messages = SupabaseClient.client.from("messages").select {
                    filter {
                        or {
                            // (Ben gönderdim VE O aldı) VEYA (O gönderdi VE Ben aldım)
                            and { eq("sender_username", currentUsername); eq("receiver_username", receiverUsername) }
                            and { eq("sender_username", receiverUsername); eq("receiver_username", currentUsername) }
                        }
                    }
                    order("created_at", Order.ASCENDING)
                }.decodeList<Message>()
            } catch (e: Exception) {}
            delay(2000) // 2 saniyede bir yenile
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Başlık
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp).background(MaterialTheme.colorScheme.surface)) {
            Text("Sohbet: @$receiverUsername", style = MaterialTheme.typography.titleLarge)
        }

        // Mesaj Listesi
        LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
            items(messages) { msg ->
                val isMe = msg.sender_username == currentUsername
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isMe) MaterialTheme.colorScheme.primaryContainer else Color.LightGray
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(4.dp).widthIn(max = 300.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            // Normal Mesaj
                            if (msg.content != null) Text(msg.content)

                            // Paylaşılan Gönderi Kartı
                            if (msg.post_id != null) {
                                Button(
                                    onClick = { /* Gönderiye git */ },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Text("Gönderiyi Gör #${msg.post_id}", color = Color.Black)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Mesaj Gönderme
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = newMessage,
                onValueChange = { newMessage = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Mesaj yaz...") }
            )
            IconButton(onClick = {
                if (newMessage.isNotEmpty()) {
                    scope.launch {
                        val msg = Message(
                            sender_username = currentUsername,
                            receiver_username = receiverUsername,
                            content = newMessage
                        )
                        SupabaseClient.client.from("messages").insert(msg)
                        newMessage = ""
                    }
                }
            }) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Gönder")
            }
        }
    }
}