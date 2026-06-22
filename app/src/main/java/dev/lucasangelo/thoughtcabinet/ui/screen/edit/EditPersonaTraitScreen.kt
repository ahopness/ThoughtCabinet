package dev.lucasangelo.thoughtcabinet.ui.screen.edit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import dev.lucasangelo.thoughtcabinet.ui.component.TypeDescriptionButton
import dev.lucasangelo.thoughtcabinet.util.darken
import dev.lucasangelo.thoughtcabinet.util.draftsDir
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
        title =
            if (traitId != null)
                "Edit The Trait Of Your Persona"
            else
                "Add A Trait To Your Persona",
        backgroundColor = viewModel.personaColorTheme.darken(),
        canGoBack = true,
        onGoBackRequest = { rootNavController.popBackStack() },
        pageCount = 2,
        initialPage = if (traitId != null) 1 else 0,
    ) { pagerState, page, offsetDistance, onNextPageRequested ->
        listOf<@Composable () -> Unit>(
            {
                EditPersonaTraitTypeSelect(
                    pageOffsetDistance = offsetDistance,
                    viewModel,
                    onNextPageRequested
                )
            },
            {
                EditPersonaTraitContent(
                    pageOffsetDistance = offsetDistance,
                    viewModel,
                    rootShowSnackbar,
                    rootNavController
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
            if (viewModel.hasLoadedTrait) {
                if (it != viewModel.traitType)
                    viewModel.traitContent = ""

                viewModel.traitType = it
                onNextPageRequested()
            }
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
            TraitTypeDescriptionButton(
                type = PersonaTraitType.TEXT,
                icon = R.drawable.icon_text,
                title = "Text",
                description = "Values, goals, quotes: Motivation",
                onTypeChanged = onTryChangeTraitType
            )
            TraitTypeDescriptionButton(
                type = PersonaTraitType.MEDIA,
                icon = R.drawable.icon_media,
                title = "Media",
                description = "Aesthetics, memories: Identity",
                onTypeChanged = onTryChangeTraitType
            )
            TraitTypeDescriptionButton(
                type = PersonaTraitType.LINK,
                icon = R.drawable.icon_link,
                title = "Link",
                description = "Songs, videos, wikis: Logic",
                onTypeChanged = onTryChangeTraitType
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
fun TraitTypeDescriptionButton(
    type: PersonaTraitType,
    icon: Int,
    title: String,
    description: String,
    onTypeChanged: (PersonaTraitType) -> Unit
) {
    TypeDescriptionButton(
        icon,
        title,
        description,
        onClick = { onTypeChanged(type) }
    )
}

@Composable
fun EditPersonaTraitContent(
    pageOffsetDistance: Float,
    viewModel: EditPersonaTraitViewModel,
    rootShowSnackbar: (String) -> Unit,
    rootNavController: NavController,
) {
    PagerScaffoldContent(pageOffsetDistance) {
        Text("Then, add the content you want to")

        val onTraitContentChanced: (String) -> Unit = {
            if (viewModel.hasLoadedTrait)
                viewModel.traitContent = it
        }

        when(viewModel.traitType) {
            PersonaTraitType.TEXT ->
                OutlinedTextField(
                    value = viewModel.traitContent,
                    onValueChange = onTraitContentChanced,
                    label = { Text("Text") },
                    maxLines = 6,
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            PersonaTraitType.MEDIA ->
                EditPersonaTraitContentImagePicker(
                    traitContent = viewModel.traitContent,
                    viewModel = viewModel,
                    onNotifyError = rootShowSnackbar,
                    onTraitContentChanced = onTraitContentChanced,
                )
            PersonaTraitType.LINK ->
                OutlinedTextField(
                    value = viewModel.traitContent,
                    onValueChange = onTraitContentChanced,
                    label = { Text("Link") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
        }

        EditPersonaFinishButton(
            viewModel,
            rootShowSnackbar,
            rootNavController
        )
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
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                val newMedia = viewModel.importMedia(uri)
                if(newMedia == null) {
                    onNotifyError("ERROR: Couldn't open selected image.")
                    return@rememberLauncherForActivityResult
                }

                onTraitContentChanced(newMedia)
            }
        }
    )

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
fun EditPersonaFinishButton(
    viewModel: EditPersonaTraitViewModel,
    rootShowSnackbar: (String) -> Unit,
    rootNavController: NavController,
) {
    val coroutineScope = rememberCoroutineScope()
    Button(onClick = {
        coroutineScope.launch {
            val currentPersona = viewModel.persona
            if (currentPersona == null) {
                rootShowSnackbar("ERROR: Persona not found.")
                return@launch
            }

            if (viewModel.traitContent.trim().isEmpty()) {
                rootShowSnackbar("ERROR: Your trait cannot be empty!")
                return@launch
            }

            if (!viewModel.commitMedia()) {
                rootShowSnackbar("ERROR: Couldn't import media.")
                return@launch
            }

            if (viewModel.trait != null) {
                viewModel.updateTrait(viewModel.trait!!, currentPersona)
                rootShowSnackbar("Trait updated successfully!")
            } else {
                viewModel.insertTrait(currentPersona)
                rootShowSnackbar("Trait added successfully!")
            }

            rootNavController.popBackStack()
        }
    }) {
        Text(
            if (viewModel.trait != null)
                "Edit Trait"
            else
                "Add Trait"
        )
    }
}