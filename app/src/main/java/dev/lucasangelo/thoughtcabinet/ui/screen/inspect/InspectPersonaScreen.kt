package dev.lucasangelo.thoughtcabinet.ui.screen.inspect

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalGridApi
import androidx.compose.foundation.layout.Grid
import androidx.compose.foundation.layout.GridFlow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
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
import dev.lucasangelo.thoughtcabinet.data.PersonaTrait
import dev.lucasangelo.thoughtcabinet.ui.component.CleanScaffold
import dev.lucasangelo.thoughtcabinet.ui.component.DeleteConfirmationDialog
import dev.lucasangelo.thoughtcabinet.ui.component.FloatingExtendedTopBar
import dev.lucasangelo.thoughtcabinet.ui.component.FloatingExtendedTopBarActionItem
import dev.lucasangelo.thoughtcabinet.ui.component.FloatingNavigationActionItem
import dev.lucasangelo.thoughtcabinet.ui.component.FloatingNavigationBar
import dev.lucasangelo.thoughtcabinet.ui.component.FloatingNavigationExpandableItem
import dev.lucasangelo.thoughtcabinet.ui.component.PersonaProfilePicture
import dev.lucasangelo.thoughtcabinet.ui.component.PersonaTraitTile
import dev.lucasangelo.thoughtcabinet.ui.component.floatingExtendedTopBarPadding
import dev.lucasangelo.thoughtcabinet.ui.component.floatingNavigationBarPadding
import dev.lucasangelo.thoughtcabinet.ui.screen.edit.EditPersonaRoute
import dev.lucasangelo.thoughtcabinet.ui.screen.edit.EditPersonaTraitRoute
import dev.lucasangelo.thoughtcabinet.util.darken
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class InspectPersonaRoute(val id: Long)

@OptIn(ExperimentalGridApi::class)
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
            initializer { InspectPersonaViewModel(dao = database.dao) }
        }
    )

    LaunchedEffect(personaId) {
        viewModel.fetchPersona(personaId)
    }

    var openPersonaDeleteAlertDialog by remember { mutableStateOf(false) }
    var pendingTraitForDeletion by remember { mutableStateOf<PersonaTrait?>(null) }

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
                        onClick = { openPersonaDeleteAlertDialog = true }
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
            item { Spacer(Modifier.height(floatingExtendedTopBarPadding)) }

            if (viewModel.persona?.traits?.isEmpty() == true)
                item {
                        Text(
                            text = "I think, therefore i am.",
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(vertical = 128.dp)
                                .fillMaxWidth(),
                        )
                }

            if (viewModel.persona?.traits?.isNotEmpty() == true)
                item {
                    Grid(
                        config = {
                            repeat(2){ column(0.5f) }
                            gap(0.dp)
                            flow = GridFlow.Row
                        },
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        viewModel.persona.let {
                            val persona = it!!

                            persona.traits.forEach { trait ->
                                val traitId = persona.traits.indexOf(trait)

                                PersonaTraitTile(
                                    type = trait.type,
                                    content = trait.content,
                                    backgroundColor = Color(persona.colorTheme)
                                ) {
                                    var expanded by remember { mutableStateOf(false) }
                                    Box(Modifier.align(Alignment.TopEnd)) {
                                        Image(
                                            painter = painterResource(R.drawable.icon_more),
                                            contentDescription = "Options",
                                            modifier = Modifier
                                                .size(48.dp)
                                                .background(
                                                    brush = Brush.radialGradient(
                                                        colors = listOf(
                                                            Color.Black.copy(0.5f),
                                                            Color.Transparent
                                                        ),
                                                        center = Offset(Float.POSITIVE_INFINITY, 0f),
                                                        radius = 125f
                                                    )
                                                )
                                                .clickable(onClick = { expanded = !expanded })
                                        )
                                        DropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false },
                                            containerColor = Color.Black,
                                            shape = RectangleShape
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Edit") },
                                                onClick = {
                                                    expanded = false
                                                    rootNavController.navigate(
                                                        EditPersonaTraitRoute(personaId, traitId)
                                                    )
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Delete", color = Color.Red.darken(0.25f)) },
                                                onClick = {
                                                    expanded = false
                                                    pendingTraitForDeletion = trait
                                                }
                                            )

                                            val lastTraitIdInList = (persona.traits.size - 1)
                                            val canMoveUp = traitId >= 2
                                            val canMoveLeft = traitId % 2 == 0 && traitId != lastTraitIdInList
                                            val canMoveRight = traitId % 2 == 1
                                            val canMoveDown = traitId <= lastTraitIdInList - 2

                                            if (canMoveUp || canMoveLeft || canMoveRight || canMoveDown)
                                                HorizontalDivider()

                                            if (canMoveUp)
                                                DropdownMenuItem(
                                                    text = { Text("Move Up") },
                                                    onClick = {
                                                        expanded = false
                                                        viewModel.moveTrait(
                                                            fromIndex = traitId,
                                                            toIndex = traitId - 2,
                                                            at = persona
                                                        )
                                                    }
                                                )
                                            if (canMoveLeft)
                                                DropdownMenuItem(
                                                    text = { Text("Move Left") },
                                                    onClick = {
                                                        expanded = false
                                                        viewModel.moveTrait(
                                                            fromIndex = traitId,
                                                            toIndex = traitId + 1,
                                                            at = persona
                                                        )
                                                    }
                                                )
                                            if (canMoveRight)
                                                DropdownMenuItem(
                                                    text = { Text("Move Right") },
                                                    onClick = {
                                                        expanded = false
                                                        viewModel.moveTrait(
                                                            fromIndex = traitId,
                                                            toIndex = traitId - 1,
                                                            at = persona
                                                        )
                                                    }
                                                )
                                            if (canMoveDown)
                                                DropdownMenuItem(
                                                    text = { Text("Move Down") },
                                                    onClick = {
                                                        expanded = false
                                                        viewModel.moveTrait(
                                                            fromIndex = traitId,
                                                            toIndex = traitId + 2,
                                                            at = persona
                                                        )
                                                    }
                                                )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(floatingNavigationBarPadding)) }
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
                            icon = R.drawable.icon_new,
                            title = "Trait",
                            showTitle = true,
                            action = { rootNavController.navigate(EditPersonaTraitRoute(personaId, null)) }
                        ),
                    )
                )
            )
        )
    }

    if (openPersonaDeleteAlertDialog) {
        DeleteConfirmationDialog(
            text = "If you delete this persona, all of their posts will be deleted as well and you won't be able recover neither.",
            onDismiss = { openPersonaDeleteAlertDialog = false },
            onConfirm = {
                coroutineScope.launch {
                    viewModel.persona?.let { viewModel.deletePersona(it) }
                    rootNavController.popBackStack()
                    rootShowSnackbar("Persona deletes successfully!")
                }
            }
        )
    }

    if (pendingTraitForDeletion != null) {
        DeleteConfirmationDialog(
            text = "If you delete this trait, you won't be able to recover it later.",
            onDismiss = { pendingTraitForDeletion = null },
            onConfirm = {
                viewModel.persona.let {
                    viewModel.deleteTrait(
                        context,
                        trait = pendingTraitForDeletion!!,
                        at = it!!
                    )
                }
            }
        )
    }
}