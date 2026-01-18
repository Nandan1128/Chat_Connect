package com.example.chatconnect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatconnect.Adaptor.UserAdapter
import com.example.chatconnect.Data_Model.User
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var userRecyclerView: RecyclerView
    private lateinit var noChatLayout: LinearLayout
    private lateinit var userList: ArrayList<User>
    private lateinit var logout_btn: ImageView
    private lateinit var adapter: UserAdapter
    private lateinit var contact_list: FloatingActionButton
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbref: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        mAuth = FirebaseAuth.getInstance()
        mDbref = FirebaseDatabase.getInstance().getReference()

        userList = ArrayList()
        adapter = UserAdapter(this, userList)

        userRecyclerView = findViewById(R.id.userRecyclerView)
        noChatLayout = findViewById(R.id.noChatLayout)
        userRecyclerView.layoutManager = LinearLayoutManager(this)
        userRecyclerView.adapter = adapter

        contact_list = findViewById(R.id.contact_list)
        logout_btn = findViewById(R.id.logout_btn)

        contact_list.setOnClickListener {
            startActivity(Intent(this@MainActivity, ContactList::class.java))
        }

        logout_btn.setOnClickListener {
            mAuth.signOut()
            val intent = Intent(this@MainActivity, login::class.java)
            finish()
            startActivity(intent)
        }

        loadActiveChats()
    }

    private fun loadActiveChats() {
        val currentUserUid = mAuth.currentUser?.uid ?: return
        
        // Listen to existing conversations for the current user
        mDbref.child("user-messages").child(currentUserUid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val talkedToUids = mutableSetOf<String>()
                    for (child in snapshot.children) {
                        child.key?.let { talkedToUids.add(it) }
                    }

                    if (talkedToUids.isEmpty()) {
                        userList.clear()
                        adapter.notifyDataSetChanged()
                        updateVisibility(true)
                        return
                    }

                    // Fetch details for these users from the 'user' node
                    mDbref.child("user").addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(userSnapshot: DataSnapshot) {
                            userList.clear()
                            for (child in userSnapshot.children) {
                                val user = child.getValue(User::class.java)
                                if (user != null && user.uid in talkedToUids) {
                                    userList.add(user)
                                }
                            }
                            adapter.notifyDataSetChanged()
                            updateVisibility(userList.isEmpty())
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("MainActivity", "Error fetching user details: ${error.message}")
                        }
                    })
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MainActivity", "Error fetching active chats: ${error.message}")
                }
            })
    }

    private fun updateVisibility(isEmpty: Boolean) {
        if (isEmpty) {
            noChatLayout.visibility = View.VISIBLE
            userRecyclerView.visibility = View.GONE
        } else {
            noChatLayout.visibility = View.GONE
            userRecyclerView.visibility = View.VISIBLE
        }
    }
}
