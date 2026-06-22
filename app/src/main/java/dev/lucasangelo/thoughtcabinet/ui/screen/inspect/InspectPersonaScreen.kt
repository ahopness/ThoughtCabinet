package dev.lucasangelo.thoughtcabinet.ui.screen.inspect

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalGridApi
import androidx.compose.foundation.layout.Grid
import androidx.compose.foundation.layout.GridFlow
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
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
import kotlin.compareTo
import kotlin.rem
import kotlin.text.compareTo

@Serializable
data class InspectPersonaRoute(val personaId: Long)

@OptIn(ExperimentalGridApi::class, ExperimentalMaterial3Api::class)
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
            initializer { InspectPersonaViewModel(dao = database.dao, application) }
        }
    )

    LaunchedEffect(personaId) {
        viewModel.fetchPersona(personaId)
    }

    var openPersonaDeleteAlertDialog by remember { mutableStateOf(false) }

    var pendingTraitInfoForManipulation by remember { mutableStateOf<Triple<Long, Int, PersonaTrait>?>(null) }
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
                                .padding(horizontal = 48.dp)
                                .padding(top = 128.dp)
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
                        viewModel.persona?.let { persona ->
                            persona.traits.forEachIndexed { traitId, trait ->
                                PersonaTraitTile(
                                    trait.type,
                                    trait.content,
                                    backgroundColor = Color(persona.colorTheme),
                                    rootShowSnackbar = rootShowSnackbar
                                ) {
                                    Image(
                                        painter = painterResource(R.drawable.icon_more),
                                        contentDescription = "Options",
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
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
                                            .clickable(onClick = { pendingTraitInfoForManipulation = Triple(personaId, traitId, trait) })
                                    )
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
                    if (!viewModel.hasLoadedPersona) return@launch
                    viewModel.deletePersona(viewModel.persona!!)
                    rootNavController.popBackStack()
                    rootShowSnackbar("Persona deleted successfully!")
                }
            }
        )
    }

    val sheetState = rememberModalBottomSheetState()
    if (pendingTraitInfoForManipulation != null) {
        InspectPersonaTraitManipulationModal(
            sheetState,
            info = pendingTraitInfoForManipulation!!,
            onDismissRequest = { pendingTraitInfoForManipulation = null },
            onDeletionRequest = { pendingTraitForDeletion = pendingTraitInfoForManipulation!!.third },
            rootNavController,
            viewModel
        )

    }
    if (pendingTraitForDeletion != null) {
        DeleteConfirmationDialog(
            text = "If you delete this trait, you won't be able to recover it later.",
            onDismiss = { pendingTraitForDeletion = null },
            onConfirm = {
                viewModel.persona?.let {
                    viewModel.deleteTrait(
                        trait = pendingTraitForDeletion!!,
                        at = it
                    )
                    rootShowSnackbar("Trait deleted successfully!")
                }
            }
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectPersonaTraitManipulationModal(
    sheetState: SheetState,
    info: Triple<Long, Int, PersonaTrait>,
    onDismissRequest: () -> Unit,
    onDeletionRequest: () -> Unit,
    rootNavController: NavController,
    viewModel: InspectPersonaViewModel,
) {
    if (!viewModel.hasLoadedPersona) return
    val persona = viewModel.persona!!
    val personaId = info.first
    val traitId = info.second

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
        containerColor = Color.Black
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            InspectPersonaTraitManipulationButton(
                action = "Edit",
                icon = R.drawable.icon_edit,
                onClick = {
                    rootNavController.navigate(
                        EditPersonaTraitRoute(personaId, traitId)
                    )
                    onDismissRequest()
                }
            )
            InspectPersonaTraitManipulationButton(
                action = "Delete",
                icon = R.drawable.icon_delete,
                color = Color.Red,
                onClick = {
                    onDeletionRequest()
                    onDismissRequest()
                },
            )

            val lastTraitIdInList = (persona.traits.size - 1)
            val canMoveUp = traitId >= 2
            val canMoveLeft = traitId % 2 == 0 && traitId != lastTraitIdInList
            val canMoveRight = traitId % 2 == 1
            val canMoveDown = traitId <= lastTraitIdInList - 2

            if (canMoveUp || canMoveLeft || canMoveRight || canMoveDown)
                Image(
                    painter = painterResource(R.drawable.divider_horizontal),
                    contentDescription = null,
                    modifier = Modifier.size(54.dp),
                )

            if (canMoveUp)
                InspectPersonaTraitManipulationButton(
                    action = "Move Up",
                    icon = R.drawable.icon_arrow_up,
                    onClick = {
                        onDismissRequest()
                        viewModel.moveTrait(
                            fromIndex = traitId,
                            toIndex = traitId - 2,
                            at = persona
                        )
                    }
                )
            if (canMoveLeft)
                InspectPersonaTraitManipulationButton(
                    action = "Move Left",
                    icon = R.drawable.icon_arrow_left,
                    onClick = {
                        onDismissRequest()
                        viewModel.moveTrait(
                            fromIndex = traitId,
                            toIndex = traitId + 1,
                            at = persona
                        )
                    }
                )
            if (canMoveRight)
                InspectPersonaTraitManipulationButton(
                    action = "Move Right",
                    icon = R.drawable.icon_back,
                    onClick = {
                        onDismissRequest()
                        viewModel.moveTrait(
                            fromIndex = traitId,
                            toIndex = traitId - 1,
                            at = persona
                        )
                    }
                )
            if (canMoveDown)
                InspectPersonaTraitManipulationButton(
                    action = "Move Down",
                    icon = R.drawable.icon_arrow_down,
                    onClick = {
                        onDismissRequest()
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
@Composable
fun InspectPersonaTraitManipulationButton(
    action: String,
    icon: Int,
    color: Color = Color.White,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            colorFilter = ColorFilter.tint(color),
            modifier = Modifier.size(54.dp)
        )
        Text(
            text = action,
            color = color
        )
    }
}
