package dev.lucasangelo.thoughtcabinet.ui.screen.inspect

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import kotlinx.serialization.Serializable

@Serializable
data class InspectPersonaRoute(val id: Long)

@Composable
fun InspectPersonaScreen(
    personaId: Long?,
    rootNavController: NavController,
    rootShowSnackbar: (String) -> Unit
) {

}