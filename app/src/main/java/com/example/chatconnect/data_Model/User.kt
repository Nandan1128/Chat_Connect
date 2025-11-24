package com.example.chatconnect.data_Model

data class User (
    var name : String? = null,
    var email: String? = null,
    var uid: String? = null,
    var phone: String? = null,
    var isRegistered: Boolean = false
)
