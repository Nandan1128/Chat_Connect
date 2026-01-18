package com.example.chatconnect.Adaptor

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatconnect.ChatActivity
import com.example.chatconnect.Data_Model.Message
import com.example.chatconnect.Data_Model.User
import com.example.chatconnect.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserAdapter(private val context: Context, private val userList: ArrayList<User>) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.txt_name)
        val lastMsgText: TextView = itemView.findViewById(R.id.tvLastmsg)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.user_layout, parent, false)
        return UserViewHolder(view)
    }

    override fun getItemCount(): Int = userList.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]

        // Display the user's name
        holder.nameText.text = user.name

        // Fetch and display the last message
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null && user.uid != null) {
            FirebaseDatabase.getInstance().getReference("user-messages")
                .child(currentUserId)
                .child(user.uid!!)
                .child("messages")
                .limitToLast(1)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (child in snapshot.children) {
                                val message = child.getValue(Message::class.java)
                                holder.lastMsgText.text = message?.message
                            }
                        } else {
                            holder.lastMsgText.text = "No messages yet"
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("UserAdapter", "Error fetching last message: ${error.message}")
                    }
                })
        }

        // On click → open chat
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("name", user.name)
            intent.putExtra("uid", user.uid)
            context.startActivity(intent)
        }
    }
}
