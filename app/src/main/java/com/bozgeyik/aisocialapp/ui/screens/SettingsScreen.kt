package com.bozgeyik.aisocialapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bozgeyik.aisocialapp.data.Profile
import com.bozgeyik.aisocialapp.data.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val currentUserEmail = SupabaseClient.client.auth.currentUserOrNull()?.email ?: ""
    val currentUsername = currentUserEmail.split("@")[0]

    // Ayarların State'leri
    var isPrivate by remember { mutableStateOf(false) }
    var messagePermission by remember { mutableStateOf("everyone") } // everyone / friends
    var allowNotifications by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(true) }

    // Verileri Çek
    LaunchedEffect(Unit) {
        val list = SupabaseClient.client.from("profiles").select {
            filter { eq("username", currentUsername) }
        }.decodeList<Profile>()

        if (list.isNotEmpty()) {
            val p = list[0]
            isPrivate = p.is_private
            messagePermission = p.message_permission
            allowNotifications = p.allow_notifications
        }
        isLoading = false
    }

    // Ayarları Kaydetme Fonksiyonu
    fun saveSettings(
        newPrivate: Boolean = isPrivate,
        newMsgPerm: String = messagePermission,
        newNotif: Boolean = allowNotifications
    ) {
        scope.launch {
            try {
                SupabaseClient.client.from("profiles").update(
                    mapOf(
                        "is_private" to newPrivate,
                        "message_permission" to newMsgPerm,
                        "allow_notifications" to newNotif
                    )
                ) { filter { eq("username", currentUsername) } }

                // State güncelle
                isPrivate = newPrivate
                messagePermission = newMsgPerm
                allowNotifications = newNotif

                Toast.makeText(context, "Ayarlar güncellendi", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Hata oluştu", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hesap Ayarları") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
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
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // BAŞLIK: GİZLİLİK
                SettingsHeader("Gizlilik ve Güvenlik")

                // 1. Gizli Hesap Anahtarı
                SettingsSwitchItem(
                    icon = Icons.Default.Lock,
                    title = "Gizli Hesap",
                    description = "Hesabını sadece seni takip edenler görebilir.",
                    checked = isPrivate,
                    onCheckedChange = { saveSettings(newPrivate = it) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // BAŞLIK: İLETİŞİM
                SettingsHeader("İletişim Tercihleri")

                // 2. Mesaj İzni (Seçenekli)
                Text("Bana kimler mesaj atabilir?", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = messagePermission == "everyone",
                        onClick = { saveSettings(newMsgPerm = "everyone") }
                    )
                    Text("Herkes")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = messagePermission == "friends",
                        onClick = { saveSettings(newMsgPerm = "friends") }
                    )
                    Text("Sadece Arkadaşlar")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Bildirimler Anahtarı
                SettingsSwitchItem(
                    icon = Icons.Default.Notifications,
                    title = "Bildirimler",
                    description = "Yeni mesaj ve takip isteklerinde bildir.",
                    checked = allowNotifications,
                    onCheckedChange = { saveSettings(newNotif = it) }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // HESAP SİLME VS.
                SettingsHeader("Hesap İşlemleri")

                Button(
                    onClick = { /* Hesap dondurma işlemi buraya eklenebilir */ },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Hesabı Dondur")
                }
            }
        }
    }
}

// Yardımcı Bileşen: Başlık
@Composable
fun SettingsHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

// Yardımcı Bileşen: Switch'li Ayar Satırı
@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.weight(1f)) {
            Icon(icon, contentDescription = null, tint = Color.Gray)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold)
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}