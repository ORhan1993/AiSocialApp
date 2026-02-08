package com.bozgeyik.aisocialapp.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.bozgeyik.aisocialapp.data.Post

@Composable
fun FeedScreen(viewModel: FeedScreenViewModel = viewModel()) {
    val posts by viewModel.posts.collectAsState()

    LazyColumn(modifier = Modifier.padding(8.dp)) {
        items(posts) { post ->
            PostCard(post = post)
        }
    }
}

@Composable
fun PostCard(post: Post) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = post.username,
                    style = MaterialTheme.typography.titleMedium
                )
                if (post.isAiGenerated) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "AI Generated",
                        tint = Color(0xFFFFD700) // Gold color
                    )
                }
            }
            AsyncImage(
                model = post.imageUrl,
                contentDescription = "Post Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "${post.likeCount} likes",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = post.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}