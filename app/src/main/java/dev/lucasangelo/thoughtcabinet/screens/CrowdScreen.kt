package dev.lucasangelo.thoughtcabinet.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable

@Serializable
object CrowdScreen

@Composable
fun CrowdScreen() {
    Text("Crowd")
}