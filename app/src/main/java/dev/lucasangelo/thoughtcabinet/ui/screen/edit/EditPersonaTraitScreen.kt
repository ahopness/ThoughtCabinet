package dev.lucasangelo.thoughtcabinet.ui.screen.edit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import dev.lucasangelo.thoughtcabinet.MainApplication
import dev.lucasangelo.thoughtcabinet.R
import dev.lucasangelo.thoughtcabinet.data.PersonaTraitType
import dev.lucasangelo.thoughtcabinet.ui.component.DeleteConfirmationDialog
import dev.lucasangelo.thoughtcabinet.ui.component.PagerScaffold
import dev.lucasangelo.thoughtcabinet.ui.component.PagerScaffoldContent
import dev.lucasangelo.thoughtcabinet.ui.component.PersonaTraitTile
import dev.lucasangelo.thoughtcabinet.ui.component.TypeDescriptionButton
import dev.lucasangelo.thoughtcabinet.util.darken
import dev.lucasangelo.thoughtcabinet.util.draftsDir
import dev.lucasangelo.thoughtcabinet.util.profilePicDir
import dev.lucasangelo.thoughtcabinet.util.traitsDir
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class EditPersonaTraitRoute(val personaId: Long, val traitId: Int?)

@Composable
fun EditPersonaTraitScreen(
    personaId: Long,
    traitId: Int?,
    rootNavController: NavController,
    rootShowSnackbar: (String) -> Unit,
) {
    val context = LocalContext.current

    val application = context.applicationContext as MainApplication
    val database = application.database
    val viewModel: EditPersonaTraitViewModel = viewModel(
        factory = viewModelFactory {
            initializer { EditPersonaTraitViewModel(database.dao, application) }
        }
    )

    LaunchedEffect(personaId, traitId) {
        viewModel.fetchPersonaAndTrait(personaId, traitId)
    }

    PagerScaffold(
        title = if (viewModel.trait != null) "Edit The Trait Of Your Persona" else "Add A Trait To Your Persona",
        backgroundColor = viewModel.colorTheme.darken(),
        canGoBack = true,
        onGoBackRequest = { rootNavController.popBackStack() },
        pageCount = 3
    ) { pagerState, page, offsetDistance, onNextPageRequested ->
        listOf<@Composable () -> Unit>(
            {
                EditPersonaTraitTypeSelect(
                    pageOffsetDistance = offsetDistance,
                    viewModel = viewModel,
                    onNextPageRequested = onNextPageRequested
                )
            },
            {
                EditPersonaTraitContent(
                    pageOffsetDistance = offsetDistance,
                    viewModel = viewModel,
                    onNotifyError = rootShowSnackbar,
                    onNextPageRequested = onNextPageRequested
                )
            },
            {
                EditPersonaTraitTypeSummary(
                    pageOffsetDistance = offsetDistance,
                    viewModel = viewModel,
                    rootNavController = rootNavController,
                    rootShowSnackbar = rootShowSnackbar,
                )
            },
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.cleanupMediaDrafts()
        }
    }
}

@Composable
fun EditPersonaTraitTypeSelect(
    pageOffsetDistance: Float,
    viewModel: EditPersonaTraitViewModel,
    onNextPageRequested: () -> Unit,
) {
    PagerScaffoldContent(pageOffsetDistance) {
        Text("First, choose the kind of trait")

        var pendingTraitTypeChange by remember { mutableStateOf<PersonaTraitType?>(null) }

        val onTraitTypeChanged: (PersonaTraitType) -> Unit = {
            if (it != viewModel.traitType) viewModel.traitContent = ""
            viewModel.traitType = it
            onNextPageRequested()
        }
        val onTryChangeTraitType: (PersonaTraitType) -> Unit = {
            if (viewModel.traitContent.isNotEmpty() &&
                it != viewModel.traitType) {
                pendingTraitTypeChange = it
            } else {
                onTraitTypeChanged(it)
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            val isEditingPersonaTrait = (viewModel.trait != null)
            TypeDescriptionButton(
                icon = R.drawable.icon_post,
                title = "Text" +
                        if (isEditingPersonaTrait && viewModel.traitType == PersonaTraitType.TEXT)
                            " " + "(current)"
                        else
                            "",
                description = "Values, goals, quotes: Motivation.",
                onClick = { onTryChangeTraitType(PersonaTraitType.TEXT) }
            )
            TypeDescriptionButton(
                icon = R.drawable.icon_media,
                title = "Media" +
                        if (isEditingPersonaTrait && viewModel.traitType == PersonaTraitType.MEDIA)
                            " " + "(current)"
                        else
                            "",
                description = "Aesthetics, memories: Identity",
                onClick = { onTryChangeTraitType(PersonaTraitType.MEDIA) }
            )
            TypeDescriptionButton(
                icon = R.drawable.icon_link,
                title = "Link" +
                        if (isEditingPersonaTrait && viewModel.traitType == PersonaTraitType.LINK)
                            " " + "(current)"
                        else
                            "",
                description = "Songs, videos, wikis: Logic",
                onClick = { onTryChangeTraitType(PersonaTraitType.LINK) }
            )
        }

        pendingTraitTypeChange?.let {
            DeleteConfirmationDialog(
                text = "The content of your trait is not empty, if you delete the content now you won't be able to recover it later.",
                onDismiss = { pendingTraitTypeChange = null },
                onConfirm = { onTraitTypeChanged(pendingTraitTypeChange!!) },
            )
        }
    }
}

@Composable
fun EditPersonaTraitContent(
    pageOffsetDistance: Float,
    viewModel: EditPersonaTraitViewModel,
    onNotifyError: (String) -> Unit,
    onNextPageRequested: () -> Unit,
) {
    PagerScaffoldContent(pageOffsetDistance) {
        Text("Then, add the content you want to")

        val onTraitContentChanced: (String) -> Unit = { viewModel.traitContent = it }
        when(viewModel.traitType) {
            PersonaTraitType.TEXT ->
                OutlinedTextField(
                    value = viewModel.traitContent,
                    onValueChange = onTraitContentChanced,
                    label = { Text("Text") },
                    maxLines = 6,
                    minLines = 4,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,

                        ),
                    modifier = Modifier.fillMaxWidth()
                )
            PersonaTraitType.MEDIA ->
                EditPersonaTraitContentImagePicker(
                    traitContent = viewModel.traitContent,
                    viewModel = viewModel,
                    onNotifyError = onNotifyError,
                    onTraitContentChanced = onTraitContentChanced,
                )
            PersonaTraitType.LINK ->
                OutlinedTextField(
                    value = viewModel.traitContent,
                    onValueChange = onTraitContentChanced,
                    label = { Text("Link") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,

                        ),
                    modifier = Modifier.fillMaxWidth()
                )
        }

        Button(onClick = onNextPageRequested) {
            Text("Next")
        }
    }
}
@Composable
fun EditPersonaTraitContentImagePicker(
    traitContent: String,
    viewModel: EditPersonaTraitViewModel,
    onNotifyError: (String) -> Unit,
    onTraitContentChanced: (String) -> Unit,
) {
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val newMedia = viewModel.importMedia(uri)
            if(newMedia == null) {
                onNotifyError("ERROR: Couldn't open selected image.")
                return@rememberLauncherForActivityResult
            }

            onTraitContentChanced(newMedia)
        }
    }

    var openAlertDialog by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        val context = LocalContext.current
        if (traitContent.isNotEmpty())
            AsyncImage(
                model = File(
                    if (viewModel.hasMediaDraft) context.cacheDir else context.filesDir,
                    (if (viewModel.hasMediaDraft) draftsDir else traitsDir) + traitContent
                ),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 8.dp)
            )
        else
            Image(
                painter = painterResource(R.drawable.icon_camera),
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
            )

        Row(
            horizontalArrangement =
                if (traitContent.isEmpty()) Arrangement.Center else Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            OutlinedButton(
                border = BorderStroke(1.dp, Color.LightGray),
                onClick = {
                    picker.launch(PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    ))
                },
            ) {
                Text("Import From Library")
            }
            if (traitContent.isNotEmpty()) {
                OutlinedButton(
                    border = BorderStroke(1.dp, Color.LightGray),
                    onClick = { openAlertDialog = true },
                ) {
                    Text("Clear Current")
                }
            }
        }
    }

    if (openAlertDialog) {
        DeleteConfirmationDialog(
            text = "If you delete the media now you won't be able to recover it later.",
            onDismiss = { openAlertDialog = false },
            onConfirm = { onTraitContentChanced("") }
        )
    }
}

@Composable
fun EditPersonaTraitTypeSummary(
    pageOffsetDistance: Float,
    viewModel: EditPersonaTraitViewModel,
    rootShowSnackbar: (String) -> Unit,
    rootNavController: NavController,
) {
    PagerScaffoldContent(pageOffsetDistance) {
        Text("Looks good?")

        PersonaTraitTile(
            type = viewModel.traitType,
            content = viewModel.traitContent,
            mediaInCache = viewModel.hasMediaDraft,
            backgroundColor = viewModel.colorTheme,
            rootShowSnackbar = rootShowSnackbar,
            modifier = Modifier
                .size(206.dp)
                .shadow(elevation = 8.dp)
        )

        val coroutineScope = rememberCoroutineScope()
        Button(onClick = {
                coroutineScope.launch {
                    val currentPersona = viewModel.persona
                    if (currentPersona == null) {
                        rootShowSnackbar("ERROR: Persona not found.")
                        return@launch
                    }

                    if (viewModel.traitContent.trim().isEmpty()) {
                        rootShowSnackbar("Your trait cannot be empty!")
                        return@launch
                    }

                    val oldTrait = viewModel.trait
                    if (oldTrait != null) {
                        viewModel.updateTrait(oldTrait, currentPersona)
                        rootShowSnackbar("Trait updated successfully!")
                    } else {
                        viewModel.insertTrait(currentPersona)
                        rootShowSnackbar("Trait added successfully!")
                    }

                    if (!viewModel.commitMedia()) {
                        rootShowSnackbar("ERROR: Couldn't import media.")
                    }

                    rootNavController.popBackStack()
                }
        }) {
            val isEditingPersonaTrait = (viewModel.trait != null)
            Text(if (isEditingPersonaTrait) "Edit Trait" else "Add Trait")
        }
    }
}