package com.example.chatconnect.data.model

data class Message(
    val id: String = "0",
    val chat_id: String,
    val sender: String,
    val content: String,
    val created_at: String?
)