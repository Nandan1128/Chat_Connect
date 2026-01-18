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
import com.example.chatconnect.Adaptor.MessageAdaptor
import com.example.chatconnect.Data_Model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatActivity : AppCompatActivity() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var backbtn: ImageView
    private lateinit var user_name_display : TextView
    private lateinit var messageBox: EditText
    private lateinit var sendButton: ImageView
    private lateinit var messageAdaptor: MessageAdaptor
    private lateinit var messageList: ArrayList<Message>
    private lateinit var mDbRef: DatabaseReference

    var receiverUid: String? = null
    var senderUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        backbtn = findViewById(R.id.back_btn)
        backbtn.setOnClickListener {
            finish()
        }
        user_name_display = findViewById(R.id.user_name_display)

        // Initialize Firebase database reference
        mDbRef = FirebaseDatabase.getInstance().getReference()

        // Get data from the Intent
        val name = intent.getStringExtra("name")
        receiverUid = intent.getStringExtra("uid")
        senderUid = FirebaseAuth.getInstance().currentUser?.uid
        Log.e("ChatActivity", "Sender UId is $senderUid")

        // Set the user's name as the title
        user_name_display.text = name

        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageBox = findViewById(R.id.messageBox)
        sendButton = findViewById(R.id.sendButton)

        // Initialize RecyclerView components
        messageList = ArrayList()
        messageAdaptor = MessageAdaptor(this, messageList)
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = messageAdaptor

        // Load messages from the user-specific path for privacy
        if (senderUid != null && receiverUid != null) {
            mDbRef.child("user-messages").child(senderUid!!).child(receiverUid!!).child("messages")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        messageList.clear()
                        for (postSnapshot in snapshot.children) {
                            val message = postSnapshot.getValue(Message::class.java)
                            if (message != null) {
                                messageList.add(message)
                            }
                        }
                        messageAdaptor.notifyDataSetChanged()
                        if (messageList.isNotEmpty()) {
                            chatRecyclerView.scrollToPosition(messageList.size - 1)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("ChatActivity", "Database error: ${error.message}")
                    }
                })
        }

        // Logic for sending a message
        sendButton.setOnClickListener {
            val messageText = messageBox.text.toString().trim()
            if (messageText.isNotEmpty() && senderUid != null && receiverUid != null) {
                val messageObject = Message(messageText, senderUid)

                // Push message to sender's view
                mDbRef.child("user-messages").child(senderUid!!).child(receiverUid!!).child("messages").push()
                    .setValue(messageObject)
                    .addOnSuccessListener {
                        // Push same message to receiver's view
                        mDbRef.child("user-messages").child(receiverUid!!).child(senderUid!!).child("messages").push()
                            .setValue(messageObject)
                    }

                // Clear the message box after sending
                messageBox.setText("")
            }
        }
    }
}
