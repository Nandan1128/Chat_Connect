package com.example.chatconnect.adaptor

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatconnect.data_Model.Message
import com.example.chatconnect.R
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class MessageAdaptor(
    val context: Context,
    val messageList: ArrayList<Message>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ITEM_SENT = 1
    private var lastAnimatedPosition = -1

    private val ITEM_RECEIVE = 2
    private val ITEM_HIDDEN = -1  // key-exchange messages

    override fun getItemViewType(position: Int): Int {

        val msg = messageList[position]

        // 🔒 Hide AES key exchange messages
        if (msg.type == "key") return ITEM_HIDDEN

        // Determine if sent or received
        return if (msg.senderUid == FirebaseAuth.getInstance().currentUser?.uid)
            ITEM_SENT else ITEM_RECEIVE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {

            ITEM_SENT -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.sent, parent, false)
                SentViewHolder(view)
            }

            ITEM_RECEIVE -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.recive, parent, false)
                ReceiveViewHolder(view)
            }

            else -> {
                // invisible key message holder
                val view = View(context)
                view.layoutParams = ViewGroup.LayoutParams(0, 0)
                object : RecyclerView.ViewHolder(view) {}
            }
        }
    }

    override fun getItemCount(): Int = messageList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val msg = messageList[position]

        // ignore key messages
        if (msg.type == "key") return

        // decrypted text OR locked placeholder
        val textToShow = msg.message ?: "🔒 Encrypted message"

        // Format timestamp
        val time = formatTime(msg.timestamp)

        // SENDER
        if (holder is SentViewHolder) {
            holder.sentMessage.text = textToShow
            holder.sentTime.text = time

            when (msg.status) {
                "sent" -> holder.tickStatus.setImageResource(R.drawable.ic_single_tick)
                "delivered" -> holder.tickStatus.setImageResource(R.drawable.ic_double_tick_grey)
                "seen" -> holder.tickStatus.setImageResource(R.drawable.ic_double_tick_blue)
            }
            runAnimation(holder.itemView, position)

        }

        // RECEIVER
        if (holder is ReceiveViewHolder) {
            holder.receiveMessage.text = textToShow
            holder.receiveTime.text = time
            runAnimation(holder.itemView, position)

        }
    }

    // Format timestamp
    private fun formatTime(ts: Long): String {
        return try {
            val date = Date(ts)
            val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
            format.format(date)
        } catch (e: Exception) {
            ""
        }
    }
    private fun runAnimation(view: View, position: Int) {
        if (position > lastAnimatedPosition) {
            val fade = AnimationUtils.loadAnimation(context, R.anim.fade_in)
            val slide = AnimationUtils.loadAnimation(context, R.anim.slide_up)

            val animation = AnimationSet(false)
            animation.addAnimation(fade)
            animation.addAnimation(slide)

            view.startAnimation(animation)
            lastAnimatedPosition = position
        }
    }


    // Sender ViewHolder
    class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sentMessage: TextView = itemView.findViewById(R.id.txt_sent_message)
        val sentTime: TextView = itemView.findViewById(R.id.txt_sent_time)
        val tickStatus: ImageView = itemView.findViewById(R.id.tick_status)
    }

    // Receiver ViewHolder
    class ReceiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receiveMessage: TextView = itemView.findViewById(R.id.txt_receive_message)
        val receiveTime: TextView = itemView.findViewById(R.id.txt_receive_time)
    }
}
