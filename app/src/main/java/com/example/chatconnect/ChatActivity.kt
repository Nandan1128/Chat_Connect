package com.example.chatconnect

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatconnect.Adaptor.MessageAdaptor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatActivity : AppCompatActivity() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageBox: EditText
    private lateinit var sendButton: ImageView
    private lateinit var messageAdaptor: MessageAdaptor
    private lateinit var messageList: ArrayList<Message>
    private lateinit var mDbRef: DatabaseReference

    var receiverRoom: String? = null
    var senderRoom: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1. SET THE CONTENT VIEW FIRST
        setContentView(R.layout.activity_chat)

        sendButton = findViewById(R.id.sendButton)
        // Initialize Firebase database reference
        mDbRef = FirebaseDatabase.getInstance().getReference()

        // Get data from the Intent
        val name = intent.getStringExtra("name")
        val receiverUid = intent.getStringExtra("uid")
        val senderUid = FirebaseAuth.getInstance().currentUser?.uid
        Log.e("ChatActivity", "Sender UId is $senderUid")

        // Create unique room IDs for the chat
        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid

        // Set the user's name as the title in the action bar
        supportActionBar?.title = name

        // 2. INITIALIZE VIEWS AFTER setContentView
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageBox = findViewById(R.id.messageBox)
        sendButton = findViewById(R.id.sendButton)

        // Initialize RecyclerView components
        messageList = ArrayList()
        messageAdaptor = MessageAdaptor(this, messageList)
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = messageAdaptor

        // 3. ADD LOGIC TO LOAD EXISTING MESSAGES
        mDbRef.child("chats").child(senderRoom!!).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear() // Clear the list to avoid duplicates
                    for (postSnapshot in snapshot.children) {
                        val message = postSnapshot.getValue(Message::class.java)
                        messageList.add(message!!)
                    }
                    messageAdaptor.notifyDataSetChanged()
                    // Optional: Scroll to the bottom to show the latest message
                    chatRecyclerView.scrollToPosition(messageList.size - 1)
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = messageAdaptor

        // Logic for adding data to recyclerView
        mDbRef.child("chats").child(senderRoom!!).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    messageList.clear()

                    for (postSnapshot in snapshot.children) {
                        val message = postSnapshot.getValue(Message::class.java)
                        messageList.add(message!!)
                    }
                    messageAdaptor.notifyDataSetChanged()

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })



        // Logic for sending a message
        sendButton.setOnClickListener {

                val messageText = messageBox.text.toString()
                if (messageText.isNotEmpty()) { // Prevent sending empty messages
                    val messageObject = Message(messageText, senderUid)


                    mDbRef.child("chats").child(senderRoom!!).child("messages").push()
                        .setValue(messageObject)
                        .addOnSuccessListener {
                            Log.e("FirebaseWrite", "Sender room write success")
                            mDbRef.child("chats").child(receiverRoom!!).child("messages").push()
                                .setValue(messageObject)
                                .addOnSuccessListener {
                                    Log.e("FirebaseWrite", "Receiver room write success")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("FirebaseWrite", "Receiver write failed: ${e.message}")
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirebaseWrite", "Sender write failed: ${e.message}")
                        }


                    // Clear the message box after sending
                    messageBox.setText("")
                }
        }
    }
}
