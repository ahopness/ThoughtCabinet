package dev.lucasangelo.thoughtcabinet.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable

@Serializable
data class PostScreen(val id: Long)

@Composable
fun PostScreen() {
    Text("Post")
}