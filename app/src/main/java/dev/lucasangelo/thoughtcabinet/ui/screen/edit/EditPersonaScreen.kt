package dev.lucasangelo.thoughtcabinet.ui.screen.edit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HueSlider
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import dev.lucasangelo.thoughtcabinet.MainApplication
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
data class EditPersonaRoute(val id: Long?)

@Composable
fun EditPersonaScreen(
    personaId: Long?,
    rootNavController: NavController,
    rootShowSnackbar: (String) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current

    val application = context.applicationContext as MainApplication
    val database = application.database
    val viewModel: EditPersonaViewModel = viewModel(
        factory = viewModelFactory {
            initializer { EditPersonaViewModel(dao = database.dao) }
        }
    )

    LaunchedEffect(personaId) {
        viewModel.fetchPersona(personaId)
    }

    PagerScaffold(
        title = if (viewModel.persona != null) "Edit Your Persona" else "Create A Persona",
        backgroundColor = viewModel.colorTheme.darken(),
        onGoBackRequest = { rootNavController.popBackStack() },
        pageCount = 5,
    ) { pagerState, page, offsetDistance, onNextPageRequested ->
        listOf<@Composable () -> Unit>(
            {
                EditPersonaIntroduction(
                    pageOffsetDistance = offsetDistance,
                    onNextPageRequested = onNextPageRequested,
                )
            },
            {
                EditPersonaProfilePicture(
                    pageOffsetDistance = offsetDistance,
                    viewModel = viewModel,
                    profilePic = viewModel.profilePic,
                    onProfilePicChanged = { if (viewModel.hasLoadedPersona) viewModel.profilePic = it },
                    onNotifyError = { rootShowSnackbar(it) },
                    onNextPageRequested = onNextPageRequested,
                )
            },
            {
                EditPersonaTextFields(
                    pageOffsetDistance = offsetDistance,
                    name = viewModel.nameText,
                    onNameChanged = { viewModel.nameText = it },
                    bio = viewModel.bioText,
                    onBioChanged = { if (viewModel.hasLoadedPersona) viewModel.bioText = it },
                    onNextPageRequested = onNextPageRequested,
                )
            },
            {
                EditPersonaColorPicker(
                    pageOffsetDistance = offsetDistance,
                    colorTheme = viewModel.colorTheme,
                    onThemeChanged = { if (viewModel.hasLoadedPersona) viewModel.colorTheme = it },
                    onNextPageRequested = onNextPageRequested,
                )
            },
            {
                EditPersonaSummary(
                    pageOffsetDistance = offsetDistance,
                    viewModel = viewModel,
                    name = viewModel.nameText,
                    bio = viewModel.bioText,
                    profilePic = viewModel.profilePic,
                    isEditingPersona = (viewModel.persona != null),
                    onCreatePersonaRequested = {
                        coroutineScope.launch {
                            if (viewModel.nameText.trim().isEmpty()) {
                                rootShowSnackbar("Your persona needs at least a name!")
                                return@launch
                            }

                            if (viewModel.persona != null) {
                                viewModel.updatePersona(viewModel.persona!!)
                                rootShowSnackbar("Persona updated successfully!")
                            } else {
                                viewModel.insertPersona()
                                rootShowSnackbar("Persona created successfully!")
                            }

                            if (!viewModel.commitProfilePic(context)) {
                                coroutineScope.launch { rootShowSnackbar("ERROR: Couldn't import profile picture.") }
                            }

                            rootNavController.popBackStack()
                        }
                    },
                )
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.cleanupProfilePicDrafts(context)
        }
    }
}

@Composable
fun EditPersonaIntroduction(
    pageOffsetDistance: Float,
    onNextPageRequested: () -> Unit,
) {
    PagerScaffoldContent(pageOffsetDistance) {
        Text(
            text = "In Thought Cabinet, instead of users, posts are made by personas",
            textAlign = TextAlign.Center,
        )

        Text(
            text =
"""Personas are the social role that one adopts: They can be things we aspire to be, specific personalities or even fictional characters.

Instead of a profile page, personas have a board which you can add personality traits to, don't forget to do that after you're done here!""",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Light,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Button(onClick = onNextPageRequested) {
            Text("Next")
        }
    }
}

@Composable
fun EditPersonaProfilePicture(
    pageOffsetDistance: Float,
    viewModel: EditPersonaViewModel,
    profilePic: String?,
    onProfilePicChanged: (String?) -> Unit,
    onNotifyError: (String) -> Unit,
    onNextPageRequested: () -> Unit,
) {
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val newProfilePic = viewModel.importProfilePic(context, uri)
            if(newProfilePic == null) {
                onNotifyError("ERROR: Couldn't open selected image.")
                return@rememberLauncherForActivityResult
            }

            onProfilePicChanged(newProfilePic)
        }
    }

    var openAlertDialog by remember { mutableStateOf(false) }

    PagerScaffoldContent(pageOffsetDistance) {
        Text("First, choose a profile picture")

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PersonaProfilePicture(
                profilePic = profilePic,
                inCache = viewModel.hasNewProfilePicDraft,
                modifier = Modifier.size(114.dp)
            )

            Spacer(Modifier.height(32.dp))

            OutlinedButton(
                border = BorderStroke(1.dp, Color.LightGray),
                onClick = {
                    picker.launch(PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        ))
                },
            ) { Text("Import From Library") }

            if (profilePic != null) {
                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    border = BorderStroke(1.dp, Color.LightGray),
                    onClick = { openAlertDialog = true },
                ) { Text("Clear Current") }
            }
        }

        Button(onClick = onNextPageRequested) {
            Text("Next")
        }
    }

    if (openAlertDialog) {
        DeleteConfirmationDialog(
            text = "If you delete the profile picture now you won't be able to recover it later.",
            onDismiss = { openAlertDialog = false },
            onConfirm = { onProfilePicChanged(null) }
        )
    }
}

@Composable
fun EditPersonaTextFields(
    pageOffsetDistance: Float,
    name: String,
    onNameChanged: (String) -> Unit,
    bio: String,
    onBioChanged: (String) -> Unit,
    onNextPageRequested: () -> Unit,
) {
    PagerScaffoldContent(pageOffsetDistance) {
        Text("Then, write some information")

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChanged,
                label = { Text("Persona's Name") },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = bio,
                onValueChange = onBioChanged,
                label = { Text("Persona's Bio") },
                maxLines = 6,
                minLines = 3,
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,

                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onNextPageRequested) { Text("Next") }
        }
    }
}

@Composable
fun EditPersonaColorPicker(
    pageOffsetDistance: Float,
    colorTheme: Color,
    onThemeChanged: (Color) -> Unit,
    onNextPageRequested: () -> Unit,
) {
    PagerScaffoldContent(pageOffsetDistance) {
        Text("Finally, pick a color theme")

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val colorPickerController = rememberColorPickerController()

            val slidersModifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .border(
                    width = 2.dp,
                    color = Color.LightGray,
                    shape = RoundedCornerShape(6.dp)
                )

            HueSlider(
                controller = colorPickerController,
                initialColor = colorTheme,
                modifier = slidersModifier
            )
            Spacer(Modifier.height(24.dp))
            BrightnessSlider(
                controller = colorPickerController,
                initialColor = colorTheme,
                modifier = slidersModifier
            )

            LaunchedEffect(colorPickerController) {
                snapshotFlow { colorPickerController.selectedColor.value }
                    .distinctUntilChanged()
                    .filter { it != Color.Transparent } // NOTE: color reset bug fix
                    .collect(onThemeChanged)
            }
        }

        Button(onClick = onNextPageRequested) {
            Text("Next")
        }
    }
}

@Composable
fun EditPersonaSummary(
    pageOffsetDistance: Float,
    viewModel: EditPersonaViewModel,
    name: String,
    bio: String,
    profilePic: String?,
    isEditingPersona: Boolean,
    onCreatePersonaRequested: () -> Unit,
) {
    PagerScaffoldContent(pageOffsetDistance) {
        Text("Looks good?")

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PersonaProfilePicture(
                profilePic = profilePic,
                inCache = viewModel.hasNewProfilePicDraft,
                modifier = Modifier.size(114.dp)
            )
            Column {
                Text(
                    text = name.ifEmpty { "(Empty)" },
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = bio.ifEmpty { "(Empty)" },
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Button(onClick = onCreatePersonaRequested) {
            Text(if (isEditingPersona) "Edit Persona" else "Create Persona")
        }
    }
}
