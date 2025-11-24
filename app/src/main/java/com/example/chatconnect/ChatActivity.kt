package com.example.chatconnect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatconnect.adaptor.MessageAdaptor
import com.example.chatconnect.data_Model.Message
import com.example.chatconnect.crypto.CryptoHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatActivity : AppCompatActivity() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var backbtn: ImageView
    private lateinit var user_name_display: TextView
    private lateinit var messageBox: EditText
    private lateinit var sendButton: ImageView
    private lateinit var messageAdaptor: MessageAdaptor
    private lateinit var messageList: ArrayList<Message>
    private lateinit var mDbRef: DatabaseReference

    private var chatId: String = ""
    private var senderUid: String = ""
    private var receiverUid: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_chat)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        // UI
        backbtn = findViewById(R.id.back_btn)
        user_name_display = findViewById(R.id.user_name_display)
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageBox = findViewById(R.id.messageBox)
        sendButton = findViewById(R.id.sendButton)

        // Firebase
        mDbRef = FirebaseDatabase.getInstance().getReference()

        // Get user details
        receiverUid = intent.getStringExtra("uid")!!
        senderUid = FirebaseAuth.getInstance().currentUser!!.uid
        val name = intent.getStringExtra("name")
        user_name_display.text = name

        // Create unique chatId (required for E2EE)
        chatId = if (senderUid < receiverUid) senderUid + receiverUid else receiverUid + senderUid

        // Generate / send AES key for this chat if needed
        ensureChatAESKey(chatId, receiverUid)

        // RecyclerView Setup
        messageList = ArrayList()
        messageAdaptor = MessageAdaptor(this, messageList)
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = messageAdaptor

        // Load & decrypt messages
        listenForMessages()

        // Back button action
        backbtn.setOnClickListener {
            startActivity(Intent(this@ChatActivity, MainActivity::class.java))
        }

        // SEND MESSAGE
        sendButton.setOnClickListener {
            val plaintext = messageBox.text.toString().trim()
            if (plaintext.isNotEmpty()) {
                sendEncryptedMessage(plaintext)
                messageBox.setText("")
            }
        }
    }

    // ===============================
    // STEP-3: CREATE AES KEY IF MISSING
    // ===============================

    private fun ensureChatAESKey(chatId: String, receiverUid: String) {

        val existingKey = CryptoHelper.getChatKey(this, chatId)
        if (existingKey != null) {
            Log.d("E2EE", "AES key already exists for chatId=$chatId")
            return
        }

        Log.d("E2EE", "No AES key → Creating new key…")

        // Fetch receiver's publicKey
        FirebaseDatabase.getInstance().getReference("user")
            .child(receiverUid)
            .child("publicKey")
            .get()
            .addOnSuccessListener { snap ->

                val publicKey = snap.value as? String
                if (publicKey == null) {
                    Log.e("E2EE", "Receiver has NO publicKey. Cannot start encrypted chat.")
                    return@addOnSuccessListener
                }

                // Create AES key
                val aesKey = CryptoHelper.generateAESKey()

                // Encrypt AES key using receiver's RSA public key
                val encryptedKey = CryptoHelper.rsaEncryptWithPublicKeyBase64(publicKey, aesKey.encoded)

                // Key-exchange message
                val keyMessage = HashMap<String, Any>()
                keyMessage["type"] = "key"
                keyMessage["senderUid"] = senderUid
                keyMessage["encryptedKey"] = encryptedKey
                keyMessage["timestamp"] = System.currentTimeMillis()

                // Push to DB
                FirebaseDatabase.getInstance().getReference("chats")
                    .child(chatId)
                    .child("messages")
                    .push()
                    .setValue(keyMessage)
                    .addOnSuccessListener {
                        CryptoHelper.storeChatKey(this, chatId, aesKey)
                        Log.d("E2EE", "AES key stored locally for chat $chatId")
                    }
            }
    }

    // ===============================
    // STEP-4: SEND ENCRYPTED MESSAGE
    // ===============================

    private fun sendEncryptedMessage(plaintext: String) {

        val aesKey = CryptoHelper.getChatKey(this, chatId)
        if (aesKey == null) {
            Log.e("E2EE", "Cannot send message → AES key missing!")
            return
        }

        // Encrypt using AES-256-GCM
        val payload = CryptoHelper.aesGcmEncrypt(aesKey, plaintext.toByteArray())

        val encryptedMsg = HashMap<String, Any>()
        encryptedMsg["type"] = "message"
        encryptedMsg["senderUid"] = senderUid
        encryptedMsg["ciphertext"] = payload.ciphertextB64
        encryptedMsg["iv"] = payload.ivB64
        encryptedMsg["timestamp"] = System.currentTimeMillis()
        encryptedMsg["status"] = "sent"


        FirebaseDatabase.getInstance().getReference("chats")
            .child(chatId)
            .child("messages")
            .push()
            .setValue(encryptedMsg)
            .addOnSuccessListener {
                Log.d("E2EE", "Encrypted message sent.")
            }
    }

    // ===============================
    // STEP-5: LISTEN & DECRYPT MESSAGES
    // ===============================

    private fun listenForMessages() {

        FirebaseDatabase.getInstance().getReference("chats")
            .child(chatId)
            .child("messages")
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    messageList.clear()

                    for (item in snapshot.children) {

                        val type = item.child("type").getValue(String::class.java)

                        when (type) {

                            // 🔐 RECEIVE AES KEY MESSAGE
                            "key" -> {
                                val encryptedKey =
                                    item.child("encryptedKey").getValue(String::class.java)

                                if (!encryptedKey.isNullOrEmpty()) {
                                    val aesKeyBytes = CryptoHelper.rsaDecryptWithPrivateKey(senderUid, encryptedKey)
                                    val aesKey =
                                        javax.crypto.spec.SecretKeySpec(aesKeyBytes, "AES")

                                    CryptoHelper.storeChatKey(this@ChatActivity, chatId, aesKey)
                                    Log.d("E2EE", "Decrypted AES key & stored for chat=$chatId")
                                }
                            }

                            // 📨 NORMAL MESSAGE (ENCRYPTED)
                            "message" -> {
                                val ciphertext = item.child("ciphertext").getValue(String::class.java)
                                val iv = item.child("iv").getValue(String::class.java)
                                val sender = item.child("senderUid").getValue(String::class.java)

                                if (!ciphertext.isNullOrEmpty() && !iv.isNullOrEmpty()) {

                                    val aesKey = CryptoHelper.getChatKey(this@ChatActivity, chatId)

                                    if (aesKey != null) {
                                        try {
                                            val plainBytes =
                                                CryptoHelper.aesGcmDecrypt(aesKey, ciphertext, iv)
                                            val plainText =
                                                String(plainBytes)

                                            // Add decrypted plaintext to list
                                            messageList.add(
                                                Message(
                                                    message = plainText,
                                                    senderUid = sender
                                                )
                                            )
                                        } catch (e: Exception) {
                                            Log.e("E2EE", "Decrypt failed: ${e.message}")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    messageAdaptor.notifyDataSetChanged()
                    if (messageList.size > 0) {
                        chatRecyclerView.scrollToPosition(messageList.size - 1)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}
