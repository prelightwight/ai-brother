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
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initializeViews()
        setupNavigation()
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
}
