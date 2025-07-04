package com.prelightwight.aibrother

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

object ThemeManager {
    
    fun applyTheme(activity: AppCompatActivity) {
        val sharedPrefs = activity.getSharedPreferences(SettingsFragment.PREFS_NAME, Context.MODE_PRIVATE)
        val isDarkTheme = sharedPrefs.getBoolean(SettingsFragment.PREF_DARK_THEME, false)
        
        if (isDarkTheme) {
            activity.setTheme(R.style.AppThemeDark)
        } else {
            activity.setTheme(R.style.AppTheme)
        }
    }
    
    fun applyFontSize(context: Context, textView: TextView) {
        val sharedPrefs = context.getSharedPreferences(SettingsFragment.PREFS_NAME, Context.MODE_PRIVATE)
        val fontSize = sharedPrefs.getInt(SettingsFragment.PREF_FONT_SIZE, 5) // Default medium
        
        val baseSize = when (textView.textSize.toInt()) {
            in 0..20 -> 14f      // Small text base
            in 21..30 -> 16f     // Medium text base  
            in 31..40 -> 18f     // Large text base
            else -> 16f          // Default medium
        }
        
        val scaledSize = when (fontSize) {
            0, 1 -> baseSize * 0.7f    // Very Small
            2, 3 -> baseSize * 0.85f   // Small
            4, 5 -> baseSize * 1.0f    // Medium (default)
            6, 7 -> baseSize * 1.15f   // Large
            else -> baseSize * 1.3f    // Very Large
        }
        
        textView.textSize = scaledSize
    }
    
    fun getMessageBubbleBackground(context: Context, isUserMessage: Boolean): Int {
        val sharedPrefs = context.getSharedPreferences(SettingsFragment.PREFS_NAME, Context.MODE_PRIVATE)
        val isRounded = sharedPrefs.getBoolean(SettingsFragment.PREF_ROUNDED_BUBBLES, true)
        
        return when {
            isUserMessage && isRounded -> R.drawable.user_message_background_rounded
            isUserMessage && !isRounded -> R.drawable.user_message_background_square
            !isUserMessage && isRounded -> R.drawable.ai_message_background_rounded
            else -> R.drawable.ai_message_background_square
        }
    }
    
    fun applyThemeToView(view: View) {
        val context = view.context
        val sharedPrefs = context.getSharedPreferences(SettingsFragment.PREFS_NAME, Context.MODE_PRIVATE)
        val isDarkTheme = sharedPrefs.getBoolean(SettingsFragment.PREF_DARK_THEME, false)
        
        // Apply theme colors to common view types
        when (view) {
            is TextView -> {
                applyFontSize(context, view)
                if (isDarkTheme) {
                    view.setTextColor(ContextCompat.getColor(context, R.color.text_primary_dark))
                } else {
                    view.setTextColor(ContextCompat.getColor(context, R.color.text_primary_light))
                }
            }
        }
    }
    
    fun isCurrentlyDarkTheme(context: Context): Boolean {
        val sharedPrefs = context.getSharedPreferences(SettingsFragment.PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getBoolean(SettingsFragment.PREF_DARK_THEME, false)
    }
    
    fun getCurrentFontSizeMultiplier(context: Context): Float {
        val sharedPrefs = context.getSharedPreferences(SettingsFragment.PREFS_NAME, Context.MODE_PRIVATE)
        val fontSize = sharedPrefs.getInt(SettingsFragment.PREF_FONT_SIZE, 5)
        
        return when (fontSize) {
            0, 1 -> 0.7f    // Very Small
            2, 3 -> 0.85f   // Small
            4, 5 -> 1.0f    // Medium (default)
            6, 7 -> 1.15f   // Large
            else -> 1.3f    // Very Large
        }
    }
    
    fun applySystemBarsTheme(activity: Activity) {
        val context = activity.applicationContext
        val sharedPrefs = context.getSharedPreferences(SettingsFragment.PREFS_NAME, Context.MODE_PRIVATE)
        val isDarkTheme = sharedPrefs.getBoolean(SettingsFragment.PREF_DARK_THEME, false)
        
        if (isDarkTheme) {
            activity.window.statusBarColor = ContextCompat.getColor(context, R.color.status_bar_dark)
            activity.window.navigationBarColor = ContextCompat.getColor(context, R.color.nav_bar_dark)
        } else {
            activity.window.statusBarColor = ContextCompat.getColor(context, R.color.status_bar_light)
            activity.window.navigationBarColor = ContextCompat.getColor(context, R.color.nav_bar_light)
        }
    }
}