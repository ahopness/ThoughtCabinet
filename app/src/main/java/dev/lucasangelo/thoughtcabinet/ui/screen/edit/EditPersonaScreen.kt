package dev.lucasangelo.thoughtcabinet.ui.screen.edit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HueSlider
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import dev.lucasangelo.thoughtcabinet.MainApplication
import dev.lucasangelo.thoughtcabinet.R
import dev.lucasangelo.thoughtcabinet.ui.component.DeleteConfirmationDialog
import dev.lucasangelo.thoughtcabinet.ui.component.PagerScaffold
import dev.lucasangelo.thoughtcabinet.ui.component.PagerScaffoldContent
import dev.lucasangelo.thoughtcabinet.ui.component.PersonaProfilePicture
import dev.lucasangelo.thoughtcabinet.util.darken
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class EditPersonaRoute(val personaId: Long?)

@Composable
fun EditPersonaScreen(
    personaId: Long?,
    rootNavController: NavController,
    rootShowSnackbar: (String) -> Unit,
) {
    val context = LocalContext.current

    val application = context.applicationContext as MainApplication
    val database = application.database
    val viewModel: EditPersonaViewModel = viewModel(
        factory = viewModelFactory {
            initializer { EditPersonaViewModel(application.repository, application) }
        }
    )

    LaunchedEffect(personaId) {
        viewModel.fetchPersona(personaId)
    }

    val animatedPersonaColorTheme by animateColorAsState(viewModel.personaColorTheme.darken())
    PagerScaffold(
        title =
            if (viewModel.persona != null)
                stringResource(R.string.edit_your_persona)
            else
                stringResource(R.string.create_a_persona),
        backgroundColor = animatedPersonaColorTheme,
        onGoBackRequest = { rootNavController.popBackStack() },
        pageCount = 1,
    ) { pagerState, page, offsetDistance, onNextPageRequested ->
        listOf<@Composable () -> Unit>(
            {
                EditPersonaContent(
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
            viewModel.cleanupProfilePicDrafts()
        }
    }
}

@Composable
fun EditPersonaContent(
    pageOffsetDistance: Float,
    viewModel: EditPersonaViewModel,
    rootShowSnackbar: (String) -> Unit,
    rootNavController: NavController,
) {
    PagerScaffoldContent(pageOffsetDistance) {
        EditPersonaProfilePicture(
            viewModel,
            onNotifyError = rootShowSnackbar
        )

        EditPersonaColorPicker(
            viewModel
        )

        EditPersonaTextFields(
            viewModel
        )

        EditPersonaFinishButton(
            viewModel,
            rootShowSnackbar,
            rootNavController
        )
    }
}
@Composable
fun EditPersonaProfilePicture(
    viewModel: EditPersonaViewModel,
    onNotifyError: (String) -> Unit,
) {
    val onProfilePicChanged: (String?) -> Unit = {
        if (viewModel.hasLoadedPersona)
            viewModel.personaProfilePic = it
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            coroutineScope.launch {
                if (uri != null) {
                    val newProfilePic = viewModel.importProfilePic(uri)
                    if(newProfilePic == null) {
                        onNotifyError(context.getString(R.string.error_could_not_open_image))
                        return@launch
                    }

                    onProfilePicChanged(newProfilePic)
                }
            }
        }
    )

    var openAlertDialog by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        PersonaProfilePicture(
            profilePic = viewModel.personaProfilePic,
            inCache = viewModel.hasNewProfilePicDraft,
            useBorder = true,
            modifier = Modifier
                .size(120.dp)
        )

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                border = BorderStroke(1.dp, Color.Gray),
                onClick = {
                    picker.launch(PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        ))
                },
            ) {
                Text(
                    if (viewModel.personaProfilePic != null)
                        stringResource(R.string.replace_profile_picture)
                    else
                        stringResource(R.string.add_profile_picture)
                )
            }

            if (viewModel.personaProfilePic != null)
                OutlinedButton(
                    border = BorderStroke(1.dp, Color.Gray),
                    onClick = { openAlertDialog = true },
                ) { Text(stringResource(R.string.clear_current)) }
        }

        if (openAlertDialog) {
            DeleteConfirmationDialog(
                text = stringResource(R.string.delete_profile_picture_warning),
                onDismiss = { openAlertDialog = false },
                onConfirm = { onProfilePicChanged(null) }
            )
        }
    }
}
@Composable
fun EditPersonaColorPicker(
    viewModel: EditPersonaViewModel,
) {
    val spacing = 16.dp
    Column(
        verticalArrangement = Arrangement.spacedBy(spacing),
        modifier = Modifier
            .border(
                border = BorderStroke(width = 1.dp, color = Color.Gray),
                shape = RoundedCornerShape(6.dp)
            )
            .padding(spacing)
    ) {
        val colorPickerController = rememberColorPickerController()

        val slidersModifier = Modifier
            .fillMaxWidth()
            .height(32.dp)

        /* NOTE: doesn't work, always set the color to red for some reason
        LaunchedEffect(viewModel.hasLoadedPersona) {
            if (viewModel.hasLoadedPersona)
                colorPickerController.selectByColor(viewModel.personaColorTheme, fromUser = false)
        }
         */

        if (viewModel.hasLoadedPersona) { // NOTE: looks janky but works, idk know how to make the transition smoother
            HueSlider(
                controller = colorPickerController,
                initialColor = viewModel.personaColorTheme,
                modifier = slidersModifier
            )

            BrightnessSlider(
                controller = colorPickerController,
                initialColor = viewModel.personaColorTheme,
                modifier = slidersModifier
            )
        }

        LaunchedEffect(colorPickerController) {
            snapshotFlow { colorPickerController.selectedColor.value }
                .distinctUntilChanged()
                .filter { it != Color.Transparent } // NOTE: color reset bug fix
                .collect({
                    if (viewModel.hasLoadedPersona)
                        viewModel.personaColorTheme = it
                })
        }
    }
}
@Composable
fun EditPersonaTextFields(
    viewModel: EditPersonaViewModel,
) {
    OutlinedTextField(
        value = viewModel.personaName,
        onValueChange = {
            if (viewModel.hasLoadedPersona)
            viewModel.personaName = it
        },
        label = { Text(stringResource(R.string.persona_name_label)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )


    OutlinedTextField(
        value = viewModel.personaBio,
        onValueChange = {
            if (viewModel.hasLoadedPersona)
                viewModel.personaBio = it
        },
        label = { Text(stringResource(R.string.persona_bio_label)) },
        maxLines = 6,
        minLines = 3,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun EditPersonaFinishButton(
    viewModel: EditPersonaViewModel,
    rootShowSnackbar: (String) -> Unit,
    rootNavController: NavController,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    Button(onClick = {
        coroutineScope.launch {
            if (viewModel.personaName.trim().isEmpty()) {
                rootShowSnackbar(context.getString(R.string.error_persona_needs_name))
                return@launch
            }

            viewModel.commitProfilePic()

            if (viewModel.persona != null) {
                viewModel.updatePersona(viewModel.persona!!)
                rootShowSnackbar(context.getString(R.string.persona_updated_success))
            } else {
                viewModel.insertPersona()
                rootShowSnackbar(context.getString(R.string.persona_created_success))
            }

            rootNavController.popBackStack()
        }
    }) {
        Text(
            if (viewModel.persona != null)
                stringResource(R.string.edit_persona)
            else
                stringResource(R.string.create_persona)
        )
    }
}