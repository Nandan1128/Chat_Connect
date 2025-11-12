package com.example.chatconnect.Adaptor

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatconnect.Message
import com.example.chatconnect.R
import com.google.firebase.auth.FirebaseAuth

class MessageAdaptor(val context: Context,val messageList : ArrayList<Message>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        val ITEM_RECEVICE = 1
        val ITEM_SENT = 2
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        if (viewType == 1){
            // Inflate Recevice
            val view: View = LayoutInflater.from(context).inflate(R.layout.recive,parent,false)
            return ReceiveViewHolder(view)
        }
        else{
            // inflate sent
            val view: View = LayoutInflater.from(context).inflate(R.layout.sent,parent,false)
            return SentViewHolder(view)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val currentMessage = messageList[position]

        if (holder.javaClass == SentViewHolder::class.java){
            //do the stuff for sent view holder

            val viewHolder = holder as SentViewHolder
            holder.sentMessage.text = currentMessage.message
        }
        else{
            //do stuff for recive view holder
            val viewHolder = holder as ReceiveViewHolder

            holder.receviceMessage.text =   currentMessage.message
        }
    }

    override fun getItemViewType(position: Int): Int {

        val currentMessage = messageList[position]

        if (FirebaseAuth.getInstance().currentUser?.uid.equals(currentMessage.senderId)){
            return ITEM_SENT
        }else{
            return ITEM_RECEVICE
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val sentMessage = itemView.findViewById<TextView>(R.id.txt_receive_message)
    }
    class ReceiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val receviceMessage = itemView.findViewById<TextView>(R.id.txt_receive_message)
    }

}