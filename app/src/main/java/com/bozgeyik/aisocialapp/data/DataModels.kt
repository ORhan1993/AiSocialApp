package com.bozgeyik.aisocialapp.data

import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String = "",
    val username: String = "",
    // Hepsine varsayılan değer atadık, veri gelmese bile uygulama yaşar:
    val full_name: String? = null,
    val bio: String? = null,
    val avatar_url: String? = null,
    val is_private: Boolean = false,
    val message_permission: String = "everyone",
    val show_status: Boolean = true,
    val allow_notifications: Boolean = true
)

@Serializable
data class Post(
    val id: Long = 0,
    val username: String,
    val description: String,
    val image_url: String?,     // Bu artık hem video hem resim URL'i tutacak
    val media_type: String = "image", // YENİ: 'image' veya 'video'
    val like_count: Int = 0,
    val created_at: String? = null,
    val is_ai_generated: Boolean = false
)

@Serializable
data class Friendship(
    val id: Long = 0,
    val sender_username: String,
    val receiver_username: String,
    val status: String
)

@Serializable
data class Comment(
    val id: Long = 0,
    val post_id: Long,
    val username: String,
    val content: String
)

@Serializable
data class Story(
    val id: Long = 0,
    val username: String,
    val image_url: String
)

@Serializable
data class Message(
    val id: Long = 0,
    val sender_username: String,
    val receiver_username: String,
    val content: String?,       // Yazı içeriği
    val media_url: String? = null, // YENİ: Ses/Resim URL
    val msg_type: String = "text", // YENİ: 'text' veya 'audio'
    val created_at: String? = null
)

@Serializable
data class Like(
    val id: Long = 0,
    val user_id: String,
    val post_id: Long
)

@Serializable
data class Notification(
    val id: Long = 0,
    val user_id: String,
    val actor_username: String,
    val type: String, // 'like', 'comment'
    val message: String,
    val created_at: String
)