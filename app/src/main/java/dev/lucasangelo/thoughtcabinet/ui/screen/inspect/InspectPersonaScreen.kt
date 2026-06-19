package dev.lucasangelo.thoughtcabinet.ui.screen.inspect

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import dev.lucasangelo.thoughtcabinet.MainApplication
import dev.lucasangelo.thoughtcabinet.R
import dev.lucasangelo.thoughtcabinet.ui.component.CleanScaffold
import dev.lucasangelo.thoughtcabinet.ui.component.FloatingExtendedTopBar
import dev.lucasangelo.thoughtcabinet.ui.component.FloatingExtendedTopBarActionItem
import dev.lucasangelo.thoughtcabinet.ui.component.FloatingNavigationActionItem
import dev.lucasangelo.thoughtcabinet.ui.component.FloatingNavigationBar
import dev.lucasangelo.thoughtcabinet.ui.component.FloatingNavigationExpandableItem
import dev.lucasangelo.thoughtcabinet.ui.component.PersonaProfilePicture
import dev.lucasangelo.thoughtcabinet.ui.component.floatingExtendedTopBarPadding
import dev.lucasangelo.thoughtcabinet.ui.component.floatingNavigationBarPadding
import dev.lucasangelo.thoughtcabinet.ui.screen.edit.EditPersonaRoute
import dev.lucasangelo.thoughtcabinet.ui.screen.edit.EditPersonaTraitRoute
import dev.lucasangelo.thoughtcabinet.util.darken
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class InspectPersonaRoute(val id: Long)

@Composable
fun InspectPersonaScreen(
    personaId: Long,
    rootNavController: NavController,
    rootShowSnackbar: (String) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current
    val application = context.applicationContext as MainApplication
    val database = application.database

    val viewModel: InspectPersonaViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                InspectPersonaViewModel(dao = database.dao)
            }
        }
    )

    LaunchedEffect(personaId) {
        viewModel.fetchPersona(personaId)
    }

    var openDeleteAlertDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    CleanScaffold(
        backgroundColor = Color(viewModel.persona?.colorTheme ?: 0).darken(),
        topBar = {
            FloatingExtendedTopBar(
                title = viewModel.persona?.name ?: "",
                description = viewModel.persona?.bio ?: "",
                canGoBack = true,
                onGoBackRequest = { rootNavController.popBackStack() },
                iconContent = { modifier, collapsedFraction ->
                    PersonaProfilePicture(
                        profilePic = viewModel.persona?.profilePic,
                        modifier = modifier
                            .scale(
                                lerp(.9f, .6f, collapsedFraction())
                            )
                            .clickable(onClick = {
                                coroutineScope
                                    .launch { listState.animateScrollToItem(0) }
                            })
                    )
                },
                actions = listOf(
                    FloatingExtendedTopBarActionItem(
                        name = "Edit",
                        icon = R.drawable.icon_edit,
                        onClick = { rootNavController.navigate(EditPersonaRoute(personaId)) }
                    ),
                    FloatingExtendedTopBarActionItem(
                        name = "Remove",
                        icon = R.drawable.icon_delete,
                        onClick = { openDeleteAlertDialog = true }
                    )
                ),
                listState = listState
            )
        }
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.fillMaxSize(),
            state = listState
        ) {
            item {
                Spacer(Modifier.height(floatingExtendedTopBarPadding))
            }

            item {
                Text(
                    text = "TODO",
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(vertical = 128.dp)
                        .height(1000.dp)
                        .fillMaxWidth()
                )
            }

            item {
                Spacer(Modifier.height(floatingNavigationBarPadding))
            }
        }

        FloatingNavigationBar(
            tabItems = emptyList(),
            actionItems = listOf(
                FloatingNavigationExpandableItem(
                    icon = R.drawable.icon_add,
                    title = "Add",
                    showTitle = false,
                    items = listOf(
                        FloatingNavigationActionItem(
                            icon = R.drawable.icon_post,
                            title = "Trait",
                            showTitle = true,
                            action = { rootNavController.navigate(EditPersonaTraitRoute(personaId)) }
                        ),
                    )
                )
            )
        )
    }

    if (openDeleteAlertDialog) {
        val onDismissRequest: () -> Unit = { openDeleteAlertDialog = false }
        AlertDialog(
            containerColor = Color.Black,
            icon = { Image(
                painter = painterResource(R.drawable.icon_delete),
                contentDescription = null,
                modifier = Modifier.size(64.dp)
            )
            },
            title = { Text("Are you sure?") },
            text = { Text("If you delete this persona, you can't recover it later.") },
            onDismissRequest = onDismissRequest,
            dismissButton = {
                Button(onClick = onDismissRequest) {
                    Text("Dismiss")
                }
            },
            confirmButton = {
                Button(onClick = {
                    coroutineScope.launch {
                        viewModel.persona?.let { viewModel.deletePersona(it) }
                        onDismissRequest()
                        rootNavController.popBackStack()
                        rootShowSnackbar("Persona deletes successfully!")
                    }
                }) {
                    Text("Confirm")
                }
            },
        )
    }
}