package com.halan.twittercounter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.halan.twittercounter.ui.screen.TwitterCounterScreen
import com.halan.twittercounter.ui.screen.TwitterCounterViewModel
import com.halan.twittercounter.ui.theme.TwitterCounterTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val viewModel: TwitterCounterViewModel by viewModels()

        setContent {
            TwitterCounterTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                TwitterCounterScreen(
                    uiState = uiState,
                    onEvent = viewModel::onEvent,
                )
            }
        }
    }
}