package dev.lucasangelo.thoughtcabinet.ui.screen

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable

@Serializable
data class PostRoute(val id: Long)

@Composable
fun PostScreen() {
    Text("Post")
}