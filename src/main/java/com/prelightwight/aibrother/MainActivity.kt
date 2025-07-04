package com.prelightwight.aibrother

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Simple button click listener for testing
        findViewById<Button>(R.id.test_button).setOnClickListener {
            Toast.makeText(this, "AI Brother is working!", Toast.LENGTH_SHORT).show()
        }
    }
}
