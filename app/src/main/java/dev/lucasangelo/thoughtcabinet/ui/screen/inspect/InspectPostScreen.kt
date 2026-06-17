package dev.lucasangelo.thoughtcabinet.ui.screen.inspect

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable

@Serializable
data class InspectPostRoute(val id: Long)

@Composable
fun InspectPostScreen() {
    Text("Post")
}