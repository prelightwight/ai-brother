package com.prelightwight.aibrother

import java.util.Date

data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val conversationId: String = "default",
    val id: String = generateId()
) {
    companion object {
        private fun generateId(): String {
            return "msg_${System.currentTimeMillis()}_${(1000..9999).random()}"
        }
        
        fun formatTimestamp(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp
            
            return when {
                diff < 60_000 -> "Just now"
                diff < 3600_000 -> "${diff / 60_000} min ago"
                diff < 86400_000 -> "${diff / 3600_000}h ago"
                diff < 604800_000 -> "${diff / 86400_000}d ago"
                else -> "Long ago"
            }
        }
    }
}