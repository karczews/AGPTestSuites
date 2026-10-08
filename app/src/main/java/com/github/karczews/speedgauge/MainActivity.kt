package com.github.karczews.speedgauge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.github.karczews.speedgauge.ui.theme.SpeedGaugeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpeedGaugeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SpeedGaugeScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}