package com.prelightwight.aibrother.core

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Utils {

    fun getCurrentTimestamp(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formatter.format(Date())
    }

    fun shortenText(text: String, maxLength: Int = 100): String {
        return if (text.length > maxLength) {
            text.take(maxLength) + "..."
        } else {
            text
        }
    }

    fun cleanInput(text: String): String {
        return text.trim().replace(Regex("\\s+"), " ")
    }
}

