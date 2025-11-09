package com.example.chatconnect.data.repository

import android.util.Log
import com.example.chatconnect.SupabaseInstance
import com.example.chatconnect.data.model.User
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.from

class AuthRepository {
    suspend fun registerUser(user: User) {
        SupabaseInstance.client.from("users").insert(user)
    }


    suspend fun findUserByUid(uid: String): User? {
        return try {
            // FIX: Filters like 'eq' now go inside the 'select' lambda.
            // .execute() is replaced with a type-safe decoding function.
            SupabaseInstance.client
                .from("users")
                .select {
                    filter {
                        eq("uid", uid)
                    }
                    limit(1) // More efficient, as you only expect one user.
                }.decodeSingleOrNull<User>() // Safely returns one user or null.
        } catch (e: RestException) {
            Log.e("AuthRepository", "Error finding user by UID: ${e.message}")
            null // Return null if there's a database/network error
        }
    }
}