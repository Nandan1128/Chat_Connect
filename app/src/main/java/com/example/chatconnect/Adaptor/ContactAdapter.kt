package com.example.chatconnect.Adaptor

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.chatconnect.ChatActivity
import com.example.chatconnect.Data_Model.Contact
import com.example.chatconnect.R

class ContactAdapter(
    private val context: Context,
    private val fullList: ArrayList<Contact>       // Full contact list
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    private val displayList: ArrayList<Contact> = ArrayList() // List shown in RecyclerView

    init {
        displayList.addAll(fullList) // Initially show all contacts
    }

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.contactName)
        val phone: TextView = itemView.findViewById(R.id.contactPhone)
        val icon: ImageView = itemView.findViewById(R.id.contactIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.contact_layout, parent, false)
        return ContactViewHolder(view)
    }

    override fun getItemCount(): Int = displayList.size

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = displayList[position]

        holder.name.text = contact.name
        holder.phone.text = contact.phone
        holder.icon.setImageResource(R.drawable.person_dark)

        holder.itemView.setOnClickListener {
            if (contact.isRegistered) {
                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra("name", contact.name)
                intent.putExtra("uid", contact.uid)     // IMPORTANT
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "Invite ${contact.name}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 🔍 Search filter function
    fun filter(text: String) {
        displayList.clear()

        if (text.isEmpty()) {
            displayList.addAll(fullList)
        } else {
            for (contact in fullList) {
                if (contact.name.lowercase().contains(text.lowercase()) ||
                    contact.phone.contains(text)
                ) {
                    displayList.add(contact)
                }
            }
        }

        notifyDataSetChanged()
    }

    // Called when full contact list updates from Firebase
    fun refresh() {
        displayList.clear()
        displayList.addAll(fullList)
        notifyDataSetChanged()
    }
}
