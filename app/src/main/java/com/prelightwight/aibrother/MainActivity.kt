package com.prelightwight.aibrother

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.prelightwight.aibrother.ui.AppRoot
import com.prelightwight.aibrother.ui.theme.AIBrotherTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AIBrotherTheme {
                AppRoot()
            }
        }
    }
}
