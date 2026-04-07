package com.halan.twittercounter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.halan.twittercounter.ui.screen.TwitterCounterScreen
import com.halan.twittercounter.ui.screen.TwitterCounterUiState
import com.halan.twittercounter.ui.theme.TwitterCounterTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            )
        )
        setContent {
            TwitterCounterTheme {
                TwitterCounterScreen(
                    uiState = TwitterCounterUiState(),
                    onEvent = {  },
                )
            }
        }
    }
}