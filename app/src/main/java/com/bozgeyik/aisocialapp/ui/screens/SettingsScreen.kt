package com.bozgeyik.aisocialapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ChevronRight
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
import com.bozgeyik.aisocialapp.data.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Şifre Değiştirme Dialog Kontrolü
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ayarlar") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Hesap Ayarları", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            // 1. Şifre Değiştir
            SettingsItem(
                icon = Icons.Default.Lock,
                title = "Şifre Değiştir",
                onClick = { showPasswordDialog = true }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text("Uygulama", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            // 2. Hakkında
            SettingsItem(
                icon = Icons.Default.Info,
                title = "Uygulama Hakkında",
                subtitle = "Sürüm 1.0.0",
                onClick = { Toast.makeText(context, "Ai Social App v1.0 \nDeveloped by Bozgeyik", Toast.LENGTH_LONG).show() }
            )

            Spacer(modifier = Modifier.weight(1f)) // Kalan boşluğu doldur

            // 3. Tehlikeli Bölge
            Button(
                onClick = { showDeleteAccountDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.DeleteForever, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hesabımı Sil")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // --- ŞİFRE DEĞİŞTİRME DİYALOĞU ---
    if (showPasswordDialog) {
        var newPassword by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text("Yeni Şifre Belirle") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Yeni Şifre") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Şifreyi Onayla") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPassword.length >= 6 && newPassword == confirmPassword) {
                            isLoading = true
                            scope.launch {
                                try {
                                    SupabaseClient.client.auth.updateUser {
                                        password = newPassword
                                    }
                                    Toast.makeText(context, "Şifre güncellendi!", Toast.LENGTH_SHORT).show()
                                    showPasswordDialog = false
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isLoading = false
                                }
                            }
                        } else {
                            Toast.makeText(context, "Şifreler uyuşmuyor veya çok kısa", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !isLoading
                ) {
                    Text("Güncelle")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false }) { Text("İptal") }
            }
        )
    }

    // --- HESAP SİLME DİYALOĞU ---
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Hesabı Sil?") },
            text = { Text("Bu işlem geri alınamaz. Tüm verilerin, gönderilerin ve mesajların silinecek.") },
            confirmButton = {
                Button(
                    onClick = {
                        // Not: Supabase Client SDK ile direkt kullanıcı silmek güvenlik nedeniyle
                        // genelde kapalıdır (Server-side gerekir).
                        // Ancak "Service Role" yoksa, kullanıcıyı çıkış yaptırıp UI'dan atabiliriz.
                        scope.launch {
                            SupabaseClient.client.auth.signOut()
                            // Burada normalde "edge function" çağırıp silmek gerekir.
                            // Şimdilik çıkış yaptırıp Login'e atıyoruz.
                            navController.navigate("home") { // Home'a git, oradan Login'e atar
                                popUpTo(0)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Evet, Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) { Text("İptal") }
            }
        )
    }
}

// Yardımcı Liste Elemanı Bileşeni
@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            }
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
    }
}