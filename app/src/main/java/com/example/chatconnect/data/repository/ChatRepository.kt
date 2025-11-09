package com.example.chatconnect.data.repository

import com.example.chatconnect.SupabaseInstance
import com.example.chatconnect.data.model.Message
import com.example.chatconnect.data.model.User
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class ChatRepository {
    suspend fun getMessages(chatId: String): List<Message> {
        // FIX: Replaced .execute() and its response with a direct .decodeList<Message>() call.
        // The 'select' block is now used for modifiers like 'order'.
        return SupabaseInstance.client
            .from("messages")
            .select {
                filter {
                    eq("chat_id", chatId)
                }
                order("created_at", order = Order.ASCENDING)
            }.decodeList<Message>()
    }

    suspend fun sendMessage(chatId: String, sender: String, content: String) {
        val message = Message(
            id = "",
            chat_id = chatId,
            sender = sender, // Matches the field name in your Message data class
            content = content,
            created_at = ""
        )
        SupabaseInstance.client.from("messages").insert(message)
    }
    suspend fun getAllUsers(currentUid: String): List<User> {
        // FIX: Replaced .execute() with .decodeList<User>().
        // Also changed the return type to List<User> for better type safety.
        return SupabaseInstance.client
            .from("users")
            .select {
                filter {
                    neq("uid", currentUid)
                }
            }.decodeList<User>()
    }
}