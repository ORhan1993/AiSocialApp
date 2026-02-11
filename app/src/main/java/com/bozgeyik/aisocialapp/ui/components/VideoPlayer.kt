package com.bozgeyik.aisocialapp.ui.components

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(videoUrl: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // ExoPlayer'ı hatırla (Her recomposition'da yeniden yaratma)
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = false // Otomatik başlama, kullanıcı tıklasın
        }
    }

    // Ekran kapanınca videoyu durdur/yok et
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    // Android View (Eski XML yapısını Compose içine gömüyoruz)
    AndroidView(
        factory = {
            PlayerView(context).apply {
                player = exoPlayer
                useController = true // Oynat/Durdur butonları görünsün
            }
        },
        modifier = modifier.fillMaxSize()
    )
}