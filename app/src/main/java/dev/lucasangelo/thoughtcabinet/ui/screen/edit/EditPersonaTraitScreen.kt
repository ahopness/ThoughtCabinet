package dev.lucasangelo.thoughtcabinet.ui.screen.edit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import dev.lucasangelo.thoughtcabinet.MainApplication
import dev.lucasangelo.thoughtcabinet.R
import dev.lucasangelo.thoughtcabinet.data.PersonaTraitType
import dev.lucasangelo.thoughtcabinet.ui.component.CleanDescriptionButton
import dev.lucasangelo.thoughtcabinet.ui.component.DeleteConfirmationDialog
import dev.lucasangelo.thoughtcabinet.ui.component.PagerScaffold
import dev.lucasangelo.thoughtcabinet.ui.component.PagerScaffoldContent
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
    val viewModel: EditPersonaTraitViewModel = viewModel(
        factory = viewModelFactory {
            initializer { EditPersonaTraitViewModel(application.repository, application) }
        }
    )

    LaunchedEffect(personaId, traitId) {
        viewModel.fetchPersonaAndTrait(personaId, traitId)
    }

    PagerScaffold(
        title =
            if (traitId != null)
                stringResource(R.string.edit_persona_trait_title)
            else
                stringResource(R.string.add_persona_trait_title),
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
        Text(stringResource(R.string.choose_trait_kind))

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
            CleanDescriptionButton(
                icon = R.drawable.icon_text,
                title = stringResource(R.string.trait_kind_text),
                description = stringResource(R.string.trait_kind_text_desc),
                onClick = { onTryChangeTraitType(PersonaTraitType.TEXT) }
            )
            CleanDescriptionButton(
                icon = R.drawable.icon_media,
                title = stringResource(R.string.trait_kind_media),
                description = stringResource(R.string.trait_kind_media_desc),
                onClick = { onTryChangeTraitType(PersonaTraitType.MEDIA) }
            )
            CleanDescriptionButton(
                icon = R.drawable.icon_link,
                title = stringResource(R.string.trait_kind_link),
                description = stringResource(R.string.trait_kind_link_desc),
                onClick = { onTryChangeTraitType(PersonaTraitType.LINK) }
            )
        }

        pendingTraitTypeChange?.let {
            DeleteConfirmationDialog(
                text = stringResource(R.string.delete_trait_content_warning),
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
    rootShowSnackbar: (String) -> Unit,
    rootNavController: NavController,
) {
    PagerScaffoldContent(pageOffsetDistance) {
        Text(stringResource(R.string.add_trait_content_prompt))

        val onTraitContentChanced: (String) -> Unit = {
            if (viewModel.hasLoadedTrait)
                viewModel.traitContent = it
        }

        when(viewModel.traitType) {
            PersonaTraitType.TEXT ->
                OutlinedTextField(
                    value = viewModel.traitContent,
                    onValueChange = onTraitContentChanced,
                    label = { Text(stringResource(R.string.trait_content_text_label)) },
                    maxLines = 6,
                    minLines = 4,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
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
                    label = { Text(stringResource(R.string.paste_link_here)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            coroutineScope.launch {
                if (uri != null) {
                    val newMedia = viewModel.importMedia(uri)
                    if(newMedia == null) {
                        onNotifyError(context.getString(R.string.error_could_not_open_image))
                        return@launch
                    }

                    onTraitContentChanced(newMedia)
                }
            }
        }
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        val context = LocalContext.current
        if (traitContent.isNotEmpty()) {
            AsyncImage(
                model = File(
                    if (viewModel.hasMediaDraft) context.cacheDir else context.filesDir,
                    (if (viewModel.hasMediaDraft) draftsDir else traitsDir) + traitContent
                ),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .border(
                        border = BorderStroke(width = 1.dp, color = Color.Gray),
                        shape = RoundedCornerShape(6.dp)
                    )
            )

            OutlinedButton(
                border = BorderStroke(1.dp, Color.Gray),
                onClick = {
                    picker.launch(PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    ))
                },
            ) {
                Text(stringResource(R.string.replace_media))
            }
        } else {
            OutlinedButton(
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(width = 1.dp, color = Color.Gray),
                modifier = Modifier.size(150.dp),
                onClick = {
                    picker.launch(PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        ))
                },
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.icon_camera),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = stringResource(R.string.add_media),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
@Composable
fun EditPersonaFinishButton(
    viewModel: EditPersonaTraitViewModel,
    rootShowSnackbar: (String) -> Unit,
    rootNavController: NavController,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    Button(onClick = {
        coroutineScope.launch {
            val currentPersona = viewModel.persona
            if (currentPersona == null) {
                rootShowSnackbar(context.getString(R.string.error_persona_not_found))
                return@launch
            }

            if (viewModel.traitContent.trim().isEmpty()) {
                rootShowSnackbar(context.getString(R.string.error_trait_cannot_empty))
                return@launch
            }

            viewModel.commitMedia()

            if (viewModel.trait != null) {
                viewModel.updateTrait(viewModel.trait!!, currentPersona)
                rootShowSnackbar(context.getString(R.string.trait_updated_success))
            } else {
                viewModel.insertTrait(currentPersona)
                rootShowSnackbar(context.getString(R.string.trait_added_success))
            }

            rootNavController.popBackStack()
        }
    }) {
        Text(
            if (viewModel.trait != null)
                stringResource(R.string.edit_trait)
            else
                stringResource(R.string.add_trait)
        )
    }
}