package dev.lucasangelo.thoughtcabinet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.lucasangelo.thoughtcabinet.ui.AppScreen
import dev.lucasangelo.thoughtcabinet.ui.ThoughtCabinetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ThoughtCabinetTheme {
                AppScreen()
            }
        }
    }
}

