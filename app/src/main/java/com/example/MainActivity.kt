package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.ProxyScreen
import com.example.ui.ProxyViewModel
import com.example.ui.theme.LetoVPNTheme

class MainActivity : ComponentActivity() {
    private val viewModel: ProxyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by viewModel.appSettings.collectAsState()
            LetoVPNTheme(themeMode = settings.themeMode) {
                ProxyScreen(viewModel = viewModel)
            }
        }
    }
}
