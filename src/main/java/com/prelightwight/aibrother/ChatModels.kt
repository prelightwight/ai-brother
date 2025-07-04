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
        val context = holder.itemView.context
        
        if (message.isUser) {
            holder.userContainer.visibility = View.VISIBLE
            holder.aiContainer.visibility = View.GONE
            holder.userMessage.text = message.content
            
            // Apply theme-aware background and font size
            val background = ThemeManager.getMessageBubbleBackground(context, true)
            holder.userMessage.setBackgroundResource(background)
            ThemeManager.applyFontSize(context, holder.userMessage)
        } else {
            holder.userContainer.visibility = View.GONE
            holder.aiContainer.visibility = View.VISIBLE
            holder.aiMessage.text = message.content
            
            // Apply theme-aware background and font size
            val background = ThemeManager.getMessageBubbleBackground(context, false)
            holder.aiMessage.setBackgroundResource(background)
            ThemeManager.applyFontSize(context, holder.aiMessage)
        }
    }
    
    override fun getItemCount() = messages.size
    
    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }
}