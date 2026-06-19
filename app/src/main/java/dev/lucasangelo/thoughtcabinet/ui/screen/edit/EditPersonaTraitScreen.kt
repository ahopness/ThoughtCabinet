package dev.lucasangelo.thoughtcabinet.ui.screen.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import dev.lucasangelo.thoughtcabinet.MainApplication
import dev.lucasangelo.thoughtcabinet.ui.component.PagerScaffold
import kotlinx.serialization.Serializable

@Serializable
data class EditPersonaTraitRoute(val id: Long)

@Composable
fun EditPersonaTraitScreen(
    personaId: Long,
    rootNavController: NavController,
    rootShowSnackbar: (String) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current
    val application = context.applicationContext as MainApplication
    val database = application.database

    val viewModel: EditPersonaTraitViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                EditPersonaTraitViewModel(dao = database.dao)
            }
        }
    )

    LaunchedEffect(personaId) {
        // TODO
    }

    PagerScaffold(
        title = "Add A Trait To Your Persona",
        backgroundColor = Color.Black, /* TODO Color(currentPersona?.colorTheme ?: 0).darken(), */
        canGoBack = true,
        onGoBackRequest = { rootNavController.popBackStack() },
        pageCount = 2
    ) { page, pagerState ->

    }
}