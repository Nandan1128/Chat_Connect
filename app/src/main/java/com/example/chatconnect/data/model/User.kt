package com.example.chatconnect.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String? = null,
    val uid: String,
    val password_hash: String? = null,
    val pseudonym: String? = null,
    val created_at: String? = null
)
