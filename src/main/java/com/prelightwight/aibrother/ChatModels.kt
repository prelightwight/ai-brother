package com.prelightwight.aibrother

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class ChatAdapter(private val messages: MutableList<ChatMessage>) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userContainer: LinearLayout = view.findViewById(R.id.user_message_container)
        val aiContainer: LinearLayout = view.findViewById(R.id.ai_message_container)
        val userMessage: TextView = view.findViewById(R.id.user_message)
        val aiMessage: TextView = view.findViewById(R.id.ai_message)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messages[position]
        
        if (message.isUser) {
            holder.userContainer.visibility = View.VISIBLE
            holder.aiContainer.visibility = View.GONE
            holder.userMessage.text = message.content
        } else {
            holder.userContainer.visibility = View.GONE
            holder.aiContainer.visibility = View.VISIBLE
            holder.aiMessage.text = message.content
        }
    }
    
    override fun getItemCount() = messages.size
    
    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }
}