package com.example.chatconnect.data_Model

data class Message(
    var type: String? = null,
    var senderUid: String? = null,
    var ciphertext: String? = null,
    var iv: String? = null,
    var encryptedKey: String? = null,
    var message: String? = null,
    var timestamp: Long = System.currentTimeMillis(),
    var status: String = "sent"
)

