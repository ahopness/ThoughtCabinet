package dev.lucasangelo.thoughtcabinet.ui.screen.inspect

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalGridApi
import androidx.compose.foundation.layout.Grid
import androidx.compose.foundation.layout.GridFlow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import dev.lucasangelo.thoughtcabinet.MainApplication
import dev.lucasangelo.thoughtcabinet.R
import dev.lucasangelo.thoughtcabinet.data.PersonaTrait
import dev.lucasangelo.thoughtcabinet.data.PersonaTraitType
import dev.lucasangelo.thoughtcabinet.ui.component.CleanIconButton
import dev.lucasangelo.thoughtcabinet.ui.component.CleanScaffold
import dev.lucasangelo.thoughtcabinet.ui.component.DeleteConfirmationDialog
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
import dev.lucasangelo.thoughtcabinet.util.LinkMetadata
import dev.lucasangelo.thoughtcabinet.util.darken
import dev.lucasangelo.thoughtcabinet.util.draftsDir
import dev.lucasangelo.thoughtcabinet.util.fetchLinkMetadata
import dev.lucasangelo.thoughtcabinet.util.traitsDir
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.io.File

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
    val viewModel: InspectPersonaViewModel = viewModel(
        factory = viewModelFactory {
            initializer { InspectPersonaViewModel(application.repository, application) }
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
                                lerp(1f, .8f, collapsedFraction())
                            )
                            .clickable(onClick = {
                                coroutineScope.launch { listState.animateScrollToItem(0) }
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
                            gap(12.dp)
                            flow = GridFlow.Row
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                    ) {
                        viewModel.persona?.let { persona ->
                            persona.traits.forEachIndexed { traitId, trait ->
                                PersonaTraitTile(
                                    trait.type,
                                    trait.content,
                                    backgroundColor = Color(persona.colorTheme),
                                    rootShowSnackbar = rootShowSnackbar,
                                    rootNavController = rootNavController
                                ) {
                                    Image(
                                        painter = painterResource(R.drawable.icon_more),
                                        contentDescription = "Options",
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(48.dp)
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
                if (!viewModel.hasLoadedPersona) return@DeleteConfirmationDialog
                viewModel.deletePersona(viewModel.persona!!)
                rootNavController.popBackStack()
                rootShowSnackbar("Persona deleted successfully!")
            }
        )
    }

    val sheetState = rememberModalBottomSheetState()
    if (pendingTraitInfoForManipulation != null) {
        InspectPersonaTraitManipulationModal(
            sheetState,
            info = pendingTraitInfoForManipulation!!,
            onDismissRequest = {
                coroutineScope.launch {
                    sheetState.hide()
                }.invokeOnCompletion {
                    pendingTraitInfoForManipulation = null
                }
            },
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
                val traitToDelete = pendingTraitForDeletion!! // NOTE: viewmodel coroutine causes race condition, taking a snapshot right before to avoid a NullPointerException
                viewModel.persona?.let {
                    viewModel.deleteTrait(
                        trait = traitToDelete,
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
            CleanIconButton(
                action = "Edit",
                icon = R.drawable.icon_edit,
                onClick = {
                    rootNavController.navigate(
                        EditPersonaTraitRoute(personaId, traitId)
                    )
                    onDismissRequest()
                }
            )
            CleanIconButton(
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
                CleanIconButton(
                    action = "Move Up",
                    icon = R.drawable.icon_arrow_up,
                    onClick = {
                        viewModel.moveTrait(
                            fromIndex = traitId,
                            toIndex = traitId - 2,
                            at = persona
                        )
                        onDismissRequest()
                    }
                )
            if (canMoveLeft)
                CleanIconButton(
                    action = "Move Left",
                    icon = R.drawable.icon_arrow_left,
                    onClick = {
                        viewModel.moveTrait(
                            fromIndex = traitId,
                            toIndex = traitId + 1,
                            at = persona
                        )
                        onDismissRequest()
                    }
                )
            if (canMoveRight)
                CleanIconButton(
                    action = "Move Right",
                    icon = R.drawable.icon_back,
                    onClick = {
                        viewModel.moveTrait(
                            fromIndex = traitId,
                            toIndex = traitId - 1,
                            at = persona
                        )
                        onDismissRequest()
                    }
                )
            if (canMoveDown)
                CleanIconButton(
                    action = "Move Down",
                    icon = R.drawable.icon_arrow_down,
                    onClick = {
                        viewModel.moveTrait(
                            fromIndex = traitId,
                            toIndex = traitId + 2,
                            at = persona
                        )
                        onDismissRequest()
                    }
                )
        }
    }
}

@Composable
fun PersonaTraitTile(
    type: PersonaTraitType,
    content: String,
    mediaInCache: Boolean = false,
    backgroundColor: Color,
    rootShowSnackbar: (String) -> Unit,
    rootNavController: NavController,
    modifier: Modifier = Modifier,
    extras: @Composable BoxScope.() -> Unit = {},
) {
    Box(
        modifier = modifier
            .aspectRatio(1f/1f)
            .background(backgroundColor.darken(0.8f))
            .clip(RoundedCornerShape(6.dp))
            .border(
                border = BorderStroke(width = 1.dp, color = Color.Gray),
                shape = RoundedCornerShape(6.dp)
            )

    ) {
        val context = LocalContext.current
        val uriHandler = LocalUriHandler.current
        when(type) {
            PersonaTraitType.TEXT ->
                Text(
                    text = "\"$content\"",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    fontStyle = FontStyle.Italic,
                    fontFamily = FontFamily.Serif,
                    modifier = Modifier.align(Alignment.Center)
                )
            PersonaTraitType.MEDIA ->
                AsyncImage(
                    model = File(
                        if (mediaInCache) context.cacheDir else context.filesDir,
                        (if (mediaInCache) draftsDir else traitsDir) + content
                    ),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .aspectRatio(1f / 1f)
                        .clickable(onClick = {
                            rootNavController.navigate(InspectMediaListRoute(listOf(content), 0, traitsDir))
                        })
                )
            PersonaTraitType.LINK ->
                Box(
                    modifier = Modifier.clickable(onClick = {
                        try {
                            uriHandler.openUri(content)
                        } catch (e: Exception) {
                            rootShowSnackbar("ERROR: Could not open URL: $content")
                        }
                    })
                ) {
                    var linkMetadata by remember(content) { mutableStateOf<LinkMetadata?>(null) }

                    LaunchedEffect(content) {
                        linkMetadata = fetchLinkMetadata(content)
                    }

                    AsyncImage(
                        model = linkMetadata?.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .aspectRatio(1f / 1f)
                            .alpha(0.5f)
                    )

                    Text(
                        text = '[' + (linkMetadata?.title ?: linkMetadata?.description ?: "LOADING...") + ']',
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    Image(
                        painter = painterResource(R.drawable.icon_redirect),
                        contentDescription = "Open Link",
                        modifier = Modifier
                            .size(54.dp)
                            .align(Alignment.BottomStart)
                    )
                }

        }

        extras()
    }
}