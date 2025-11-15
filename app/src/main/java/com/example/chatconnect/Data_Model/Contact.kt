package com.example.chatconnect.Data_Model

data class Contact(
    val name: String,
    val phone: String,
    var isRegistered: Boolean = false,
    var uid: String? = null
)
