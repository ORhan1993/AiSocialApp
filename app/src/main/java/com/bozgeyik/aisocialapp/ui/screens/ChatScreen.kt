package com.bozgeyik.aisocialapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.bozgeyik.aisocialapp.data.Message
import com.bozgeyik.aisocialapp.data.SupabaseClient
import com.bozgeyik.aisocialapp.utils.DateUtils
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(receiverUsername: String) {
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var newMessageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val currentUser = SupabaseClient.client.auth.currentUserOrNull()
    val currentUsername = currentUser?.email?.split("@")?.get(0) ?: ""

    // 1. MESAJLARI YÜKLE VE CANLI DİNLE
    LaunchedEffect(receiverUsername) {
        // A. Önce mevcut mesajları çek
        val initialMessages = SupabaseClient.client.from("messages").select {
            filter {
                or {
                    and {
                        eq("sender_username", currentUsername)
                        eq("receiver_username", receiverUsername)
                    }
                    and {
                        eq("sender_username", receiverUsername)
                        eq("receiver_username", currentUsername)
                    }
                }
            }
            order("created_at", Order.ASCENDING)
        }.decodeList<Message>()

        messages = initialMessages
        // En alta kaydır
        if (messages.isNotEmpty()) {
            listState.scrollToItem(messages.size - 1)
        }

        // B. CANLI DİNLEME (Realtime)
        val channel = SupabaseClient.client.channel("messages") // Kanal oluştur

        val changeFlow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "messages" // Tablo adı
        }

        changeFlow.onEach {
            // "it" burada PostgresAction.Insert türündedir
            val newRecord = it.decodeRecord<Message>() // Kütüphane eklenince bu çalışacak

            if ((newRecord.sender_username == receiverUsername && newRecord.receiver_username == currentUsername) ||
                (newRecord.sender_username == currentUsername && newRecord.receiver_username == receiverUsername)) {

                // Ambiguity hatası burada çözülecek çünkü newRecord artık kesin olarak 'Message' türünde
                messages = messages + newRecord

                scope.launch {
                    listState.animateScrollToItem(messages.size)
                }
            }
        }.launchIn(this)

        channel.subscribe()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(receiverUsername.take(1).uppercase(), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(receiverUsername)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5)) // WhatsApp gibi hafif gri arka plan
        ) {
            // MESAJ LİSTESİ
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messages) { msg ->
                    val isMe = msg.sender_username == currentUsername
                    MessageBubble(message = msg, isMe = isMe)
                }
            }

            // MESAJ YAZMA ALANI
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newMessageText,
                    onValueChange = { newMessageText = it },
                    placeholder = { Text("Mesaj yaz...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                FloatingActionButton(
                    onClick = {
                        if (newMessageText.isNotEmpty()) {
                            scope.launch {
                                val msgToSend = Message(
                                    sender_username = currentUsername,
                                    receiver_username = receiverUsername,
                                    content = newMessageText
                                )
                                // Mesajı gönder (Realtime sayesinde listeye otomatik düşecek!)
                                SupabaseClient.client.from("messages").insert(msgToSend)
                                newMessageText = ""
                            }
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Gönder")
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message, isMe: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isMe) MaterialTheme.colorScheme.primary else Color.White,
            shape = if (isMe) RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp)
            else RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp),
            shadowElevation = 1.dp,
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp).widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = message.content ?: "",
                    color = if (isMe) Color.White else Color.Black,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = DateUtils.timeAgo(message.created_at), // YENİ: Zaman formatı
                    color = if (isMe) Color.White.copy(alpha = 0.7f) else Color.Gray,
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}