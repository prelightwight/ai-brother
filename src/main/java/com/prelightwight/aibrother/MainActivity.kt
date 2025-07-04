package com.prelightwight.aibrother

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    
    private lateinit var statusText: TextView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var tutorialManager: TutorialManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before calling super.onCreate()
        ThemeManager.applyTheme(this)
        
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Apply system bars theme
        ThemeManager.applySystemBarsTheme(this)
        
        initializeViews()
        setupNavigation()
        applyThemeToViews()
        
        // Initialize tutorial system
        tutorialManager = TutorialManager(this)
        
        // Start tutorial on first launch (with slight delay for layout)
        statusText.postDelayed({
            tutorialManager.startTutorial()
        }, 500)
    }
    
    private fun initializeViews() {
        statusText = findViewById(R.id.status_text)
        bottomNavigation = findViewById(R.id.bottom_navigation)
    }
    
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        
        bottomNavigation.setupWithNavController(navController)
        
        // Update status text based on current destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            statusText.text = when (destination.id) {
                R.id.nav_chat -> "Ready"
                R.id.nav_brain -> "Brain Mode"
                R.id.nav_files -> "Files Mode"
                R.id.nav_images -> "Images Mode"
                R.id.nav_settings -> "Settings Mode"
                else -> "Ready"
            }
        }
    }
    
    private fun applyThemeToViews() {
        // Apply theme to header text
        ThemeManager.applyThemeToView(statusText)
        
        // Find header title and apply theme
        val headerTitle = findViewById<TextView>(R.id.header_title)
        headerTitle?.let { ThemeManager.applyThemeToView(it) }
    }
    
    override fun onResume() {
        super.onResume()
        // Reapply theme in case settings changed
        applyThemeToViews()
    }
}
