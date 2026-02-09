package com.bozgeyik.aisocialapp.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.bozgeyik.aisocialapp.data.Story
import com.bozgeyik.aisocialapp.data.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@Composable
fun AddStoryScreen(onStoryAdded: () -> Unit) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Galeri Açıcı
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        imageUri = it
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Hikaye Paylaş", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        // Resim Seçme Alanı
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.LightGray)
                .clickable { launcher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Resim Seçmek İçin Dokun", color = Color.DarkGray)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    if (imageUri != null) {
                        isLoading = true
                        scope.launch {
                            try {
                                // 1. Resmi Byte'lara çevir
                                val byteArray = withContext(Dispatchers.IO) {
                                    context.contentResolver.openInputStream(imageUri!!)?.use { it.readBytes() }
                                }

                                if (byteArray != null) {
                                    // 2. Storage'a Yükle
                                    val fileName = "story_${UUID.randomUUID()}.jpg"
                                    val bucket = SupabaseClient.client.storage.from("images")
                                    bucket.upload(fileName, byteArray)
                                    val url = bucket.publicUrl(fileName)

                                    // 3. Veritabanına Yaz
                                    val userEmail = SupabaseClient.client.auth.currentUserOrNull()?.email ?: "Anonim"
                                    val username = userEmail.split("@")[0]

                                    val newStory = Story(username = username, image_url = url)
                                    SupabaseClient.client.from("stories").insert(newStory)

                                    Toast.makeText(context, "Hikaye Paylaşıldı!", Toast.LENGTH_SHORT).show()
                                    onStoryAdded() // Geri dön
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Hata: ${e.message}", Toast.LENGTH_LONG).show()
                            } finally {
                                isLoading = false
                            }
                        }
                    } else {
                        Toast.makeText(context, "Lütfen bir resim seçin.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Hikayeni Paylaş")
            }
        }
    }
}