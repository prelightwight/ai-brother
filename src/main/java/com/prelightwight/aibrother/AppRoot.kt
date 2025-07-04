package com.prelightwight.aibrother

import androidx.compose.runtime.Composable
import com.prelightwight.aibrother.ui.theme.AIBrotherTheme
import com.prelightwight.aibrother.ui.navigation.NavGraph

@Composable
fun AppRoot() {
    AIBrotherTheme {
        NavGraph()
    }
}

