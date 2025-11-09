package com.example.chatconnect.viewmodel

import androidx.lifecycle.ViewModel
import com.example.chatconnect.data.model.User
import com.example.chatconnect.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel() {
    private val repo = AuthRepository()

    private val _isRegistered = MutableStateFlow(false)
    val isRegistered: StateFlow<Boolean> = _isRegistered

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess

    fun register(user: User) {
        viewModelScope.launch {
            try {
                repo.registerUser(user)
                _isRegistered.value = true
            } catch (e: Exception) {
                e.printStackTrace()
                _isRegistered.value = false
            }
        }
    }

    fun login(uid: String, password: String) {
        viewModelScope.launch {
            try {
                val users = repo.loginUser(uid)
                if (users.isNotEmpty() && users.first().password_hash == password) {
                    _loginSuccess.value = true
                } else {
                    _loginSuccess.value = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _loginSuccess.value = false
            }
        }
    }
}