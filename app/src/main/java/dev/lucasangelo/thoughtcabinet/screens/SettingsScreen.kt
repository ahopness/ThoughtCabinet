package dev.lucasangelo.thoughtcabinet.screens

import androidx.compose.foundation.Image
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import dev.lucasangelo.thoughtcabinet.R
import kotlinx.serialization.Serializable

@Serializable
object SettingsScreen

@Composable
fun SettingsScreen() {
    Text("Settings")
}