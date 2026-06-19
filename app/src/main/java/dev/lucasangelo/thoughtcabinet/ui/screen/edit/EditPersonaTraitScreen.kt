package dev.lucasangelo.thoughtcabinet.ui.screen.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.toColor
import androidx.navigation.NavController
import dev.lucasangelo.thoughtcabinet.data.AppDao
import dev.lucasangelo.thoughtcabinet.data.AppDatabase
import dev.lucasangelo.thoughtcabinet.data.PersonaEntity
import dev.lucasangelo.thoughtcabinet.ui.component.PagerScaffold
import dev.lucasangelo.thoughtcabinet.util.darken
import kotlinx.serialization.Serializable

@Serializable
data class EditPersonaTraitRoute(val id: Long)

@Composable
fun EditPersonaTraitScreen(
    personaId: Long,
    rootNavController: NavController,
    rootShowSnackbar: (String) -> Unit,
    database: AppDao,
) {
    val coroutineScope = rememberCoroutineScope()

    var currentPersona by remember { mutableStateOf<PersonaEntity?>(null) }
    LaunchedEffect(personaId) {
        currentPersona = database.getPersona(personaId)
    }

    PagerScaffold(
        title = "Add A Trait To Your Persona",
        backgroundColor = Color(currentPersona?.colorTheme ?: 0).darken(),
        canGoBack = true,
        onGoBackRequest = { rootNavController.popBackStack() },
        pageCount = 2
    ) { page, pagerState ->

    }
}