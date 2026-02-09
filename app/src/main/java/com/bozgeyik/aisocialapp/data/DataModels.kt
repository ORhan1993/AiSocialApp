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
    val image_url: String?,
    val is_ai_generated: Boolean,
    val like_count: Int = 0
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
    val content: String?,
    val post_id: Long? = null,
    val created_at: String? = null
)