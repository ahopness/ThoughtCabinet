package dev.lucasangelo.thoughtcabinet.ui.screen.edit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.res.painterResource
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
import dev.lucasangelo.thoughtcabinet.ui.component.PagerScaffold
import dev.lucasangelo.thoughtcabinet.ui.component.PersonaProfilePicture
import dev.lucasangelo.thoughtcabinet.util.cleanupDrafts
import dev.lucasangelo.thoughtcabinet.util.copyUriToInternalStorage
import dev.lucasangelo.thoughtcabinet.util.darken
import dev.lucasangelo.thoughtcabinet.util.draftsDir
import dev.lucasangelo.thoughtcabinet.util.getFileExtension
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class EditPersonaRoute(val id: Long?)

// TODO: add animations

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
        pageCount = 4,
    ) { page, pagerState ->
        when (page) {
            0 -> {
                EditPersonaProfilePicture(
                    viewModel = viewModel,
                    profilePic = viewModel.profilePic,
                    onProfilePicChanged = { if (viewModel.hasLoadedPersona) viewModel.profilePic = it },
                    onNotifyError = {
                        coroutineScope.launch { rootShowSnackbar(it) }
                    },
                    onNextPageRequested = {
                        coroutineScope.launch { pagerState.animateScrollToPage(1) }
                    },
                )
            }
            1 -> {
                EditPersonaTextFields(
                    name = viewModel.nameText,
                    onNameChanged = { viewModel.nameText = it },
                    bio = viewModel.bioText,
                    onBioChanged = { if (viewModel.hasLoadedPersona) viewModel.bioText = it },
                    onNextPageRequested = {
                        coroutineScope.launch { pagerState.animateScrollToPage(2) }
                    },
                )
            }
            2 -> {
                EditPersonaColorPicker(
                    colorTheme = viewModel.colorTheme,
                    onThemeChanged = { if (viewModel.hasLoadedPersona) viewModel.colorTheme = it },
                    onNextPageRequested = {
                        coroutineScope.launch { pagerState.animateScrollToPage(3) }
                    },
                )
            }
            3 -> {
                EditPersonaSummary(
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
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.cleanupProfilePicDrafts(context)
        }
    }
}

@Composable
fun EditPersonaProfilePicture(
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

    Box(Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text("Choose A Profile Picture")
            Spacer(Modifier.height(32.dp))

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

            Spacer(Modifier.height(84.dp))
            Button(onClick = onNextPageRequested) {
                Text("Next")
            }

        }
    }

    if (openAlertDialog) {
        val onDismissRequest: () -> Unit = { openAlertDialog = false }
        AlertDialog(
            containerColor = Color.Black,
            icon = { Image(
                painter = painterResource(R.drawable.icon_delete),
                contentDescription = null,
                modifier = Modifier.size(64.dp)
                )
            },
            title = { Text("Are you sure?") },
            text = { Text("If you delete the profile picture now, you can't recover it later.") },
            onDismissRequest = onDismissRequest,
            dismissButton = {
                Button(onClick = onDismissRequest) {
                    Text("Dismiss")
                }
            },
            confirmButton = {
                Button(onClick = { onProfilePicChanged(null); onDismissRequest() }) {
                    Text("Confirm")
                }
            },
        )
    }
}

@Composable
fun EditPersonaTextFields(
    name: String,
    onNameChanged: (String) -> Unit,
    bio: String,
    onBioChanged: (String) -> Unit,
    onNextPageRequested: () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp)
        ) {
            Text("Write some information")
            Spacer(Modifier.height(32.dp))

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

            Spacer(Modifier.height(84.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = onNextPageRequested) { Text("Next") }
            }
        }
    }
}

@Composable
fun EditPersonaColorPicker(
    colorTheme: Color,
    onThemeChanged: (Color) -> Unit,
    onNextPageRequested: () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp)
        ) {
            Text("Pick A Color")
            Spacer(Modifier.height(32.dp))

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

            Spacer(Modifier.height(84.dp))
            Button(onClick = onNextPageRequested) {
                Text("Next")
            }
        }
    }
}

@Composable
fun EditPersonaSummary(
    viewModel: EditPersonaViewModel,
    name: String,
    bio: String,
    profilePic: String?,
    isEditingPersona: Boolean,
    onCreatePersonaRequested: () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp)
        ) {
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

            Spacer(Modifier.height(64.dp))
            Button(onClick = onCreatePersonaRequested) {
                Text(if (isEditingPersona) "Update Persona" else "Create Persona")
            }
        }
    }
}
