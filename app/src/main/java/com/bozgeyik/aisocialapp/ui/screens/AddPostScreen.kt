package com.bozgeyik.aisocialapp.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bozgeyik.aisocialapp.data.Post
import com.bozgeyik.aisocialapp.data.SupabaseClient
import com.bozgeyik.aisocialapp.ui.components.VideoPlayer
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPostScreen(onPostAdded: () -> Unit) {
    var description by remember { mutableStateOf("") }
    var mediaUri by remember { mutableStateOf<Uri?>(null) }
    var mediaType by remember { mutableStateOf("image") } // 'image' veya 'video'
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Resim Seçici
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            mediaUri = uri
            mediaType = "image"
        }
    }

    // Video Seçici
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            mediaUri = uri
            mediaType = "video"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Yeni Gönderi") })
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // MEDYA SEÇİM ALANI
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Color.LightGray, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (mediaUri != null) {
                    if (mediaType == "image") {
                        AsyncImage(
                            model = mediaUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Seçilen videoyu oynat (Önizleme)
                        VideoPlayer(videoUrl = mediaUri.toString())
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Medyayı Seçin", color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row {
                            Button(onClick = { imagePicker.launch("image/*") }) {
                                Icon(Icons.Default.Image, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Fotoğraf")
                            }
                            Spacer(Modifier.width(16.dp))
                            Button(onClick = { videoPicker.launch("video/*") }) {
                                Icon(Icons.Default.Videocam, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Video")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Açıklama yaz...") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (mediaUri != null && description.isNotEmpty()) {
                        isLoading = true
                        scope.launch {
                            try {
                                val user = SupabaseClient.client.auth.currentUserOrNull()
                                val username = user?.email?.split("@")?.get(0) ?: "anonim"

                                val fileName = "${mediaType}_${UUID.randomUUID()}.${if(mediaType=="image") "jpg" else "mp4"}"
                                val bucket = SupabaseClient.client.storage.from("images") // "images" bucketını kullanmaya devam ediyoruz

                                val bytes = withContext(Dispatchers.IO) {
                                    context.contentResolver.openInputStream(mediaUri!!)?.use { it.readBytes() }
                                }

                                if (bytes != null) {
                                    bucket.upload(fileName, bytes)
                                    val url = bucket.publicUrl(fileName)

                                    val newPost = Post(
                                        username = username,
                                        description = description,
                                        image_url = url,
                                        media_type = mediaType // 'video' veya 'image' olarak kaydediyoruz
                                    )

                                    SupabaseClient.client.from("posts").insert(newPost)

                                    Toast.makeText(context, "Paylaşıldı!", Toast.LENGTH_SHORT).show()
                                    onPostAdded()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Hata: ${e.message}", Toast.LENGTH_LONG).show()
                            } finally {
                                isLoading = false
                            }
                        }
                    } else {
                        Toast.makeText(context, "Lütfen medya ve açıklama girin", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Paylaş")
            }
        }
    }
}