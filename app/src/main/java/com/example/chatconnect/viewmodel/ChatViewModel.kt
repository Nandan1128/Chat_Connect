package com.example.chatconnect.viewmodel

import androidx.lifecycle.ViewModel
import com.example.chatconnect.data.model.Message
import com.example.chatconnect.data.repository.ChatRepository
import kotlinx.coroutines.flow.StateFlow

class ChatViewModel : ViewModel{
    private val repo = ChatRepository()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    fun loadMessages(chatId: String) {
        viewModelScope.launch {
            try {
                _messages.value = repo.getMessages(chatId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendMessage(chatId: String, sender: String, content: String) {
        viewModelScope.launch {
            try {
                repo.sendMessage(chatId, sender, content)
                loadMessages(chatId) // refresh
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}