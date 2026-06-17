package dev.lucasangelo.thoughtcabinet.ui.screen.edit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.toColor
import androidx.navigation.NavController
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.ColorPickerController
import com.github.skydoves.colorpicker.compose.HueSlider
import com.github.skydoves.colorpicker.compose.SaturationSlider
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import dev.lucasangelo.thoughtcabinet.R
import dev.lucasangelo.thoughtcabinet.data.AppDatabase
import dev.lucasangelo.thoughtcabinet.data.PersonaEntity
import dev.lucasangelo.thoughtcabinet.ui.component.PagerScaffold
import dev.lucasangelo.thoughtcabinet.ui.component.PersonaProfilePicture
import dev.lucasangelo.thoughtcabinet.util.copyUriToInternalStorage
import dev.lucasangelo.thoughtcabinet.util.darken
import dev.lucasangelo.thoughtcabinet.util.deleteInternalStorageFile
import dev.lucasangelo.thoughtcabinet.util.getFileExtension
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.io.File
import java.time.Instant
import java.util.UUID

@Serializable
data class EditPersonaRoute(val id: Long?)

// TODO: add animations

@Composable
fun EditPersonaScreen(
    personaId: Long?,
    rootNavController: NavController,
    rootShowSnackbar: (String) -> Unit
) {
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()
    val database = remember { AppDatabase.getInstance(context).dao }

    var hasLoadedPersona by remember { mutableStateOf(false) }
    var currentPersona by remember { mutableStateOf<PersonaEntity?>(null) }

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var profilePic by remember { mutableStateOf<String?>(null) }
    var colorTheme by remember { mutableStateOf(Color.Black) }

    LaunchedEffect(personaId) {
        currentPersona = if (personaId != null) database.getPersona(personaId) else null
        currentPersona?.let {
            name = it.name
            description = it.description
            profilePic = it.profilePic
            colorTheme = Color(it.colorTheme)
        }
        hasLoadedPersona = true
    }

    PagerScaffold(
        title = if (currentPersona != null) "Edit Your Persona" else "Create A Persona",
        backgroundColor = colorTheme.darken(),
        onGoBackRequest = { rootNavController.popBackStack() },
        pageCount = 4,
    ) { page, pagerState ->
        when (page) {
            0 -> {
                EditPersonaProfilePicture(
                    profilePic = profilePic,
                    onProfilePicChanged = { if (hasLoadedPersona) profilePic = it },
                    name = name,
                    onNotifyError = {
                        coroutineScope.launch {
                            rootShowSnackbar(it)
                        }
                    },
                    onNextPageRequested = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    },
                )
            }
            1 -> {
                EditPersonaTextFields(
                    name = name,
                    onNameChanged = { name = it },
                    description = description,
                    onDescriptionChanged = { if (hasLoadedPersona) description = it },
                    onNextPageRequested = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(2)
                        }
                    },
                )
            }
            2 -> {
                EditPersonaColorPicker(
                    colorTheme = colorTheme,
                    onThemeChanged = { if (hasLoadedPersona) colorTheme = it },
                    onNextPageRequested = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(3)
                        }
                    },
                )
            }
            3 -> {
                EditPersonaSummary(
                    name = name,
                    description = description,
                    profilePic = profilePic,
                    isEditingPersona = (currentPersona != null),
                    onCreatePersonaRequested = {
                        coroutineScope.launch {
                            if (name.trim().isEmpty()) {
                                rootShowSnackbar("Your persona needs at least a name!")
                                return@launch
                            }

                            val basePersona = PersonaEntity(
                                id = currentPersona?.id ?: 0,
                                createdAt = currentPersona?.createdAt ?: Instant.now(),
                                updatedAt = if (currentPersona != null) Instant.now() else null,
                                name = name.trim(),
                                description = description.trim(),
                                profilePic = profilePic,
                                colorTheme = colorTheme.toArgb(),
                                inspirations = currentPersona?.inspirations ?: emptyList(),
                                metadata = currentPersona?.metadata ?: emptyMap()
                            )

                            if (currentPersona != null) {
                                database.updatePersona(basePersona)
                                rootShowSnackbar("Persona updated successfully!")
                            } else {
                                database.insertPersona(basePersona)
                                rootShowSnackbar("Persona created successfully!")
                            }

                            rootNavController.popBackStack()
                        }
                    },
                )
            }
        }
    }

    // NOTE: the 'Clear Current' button only sets the variable to null
    // because if the user exists this screen without saving after deleting
    // a pfp, the path the variable is pointing to gets invalidated
    DisposableEffect(Unit) {
        onDispose {
            coroutineScope.launch {
                val referencedProfilePics = database.getAllPersonas()
                    .first()
                    .mapNotNull { it.profilePic }
                    .toSet()

                val folder = File(context.filesDir, "profile-pictures")
                folder.listFiles()
                    ?.filter { it.isFile && it.name !in referencedProfilePics }
                    ?.forEach { it.delete() }
            }
        }
    }
}

@Composable
fun EditPersonaProfilePicture(
    profilePic: String?,
    onProfilePicChanged: (String?) -> Unit,
    name: String,
    onNotifyError: (String) -> Unit,
    onNextPageRequested: () -> Unit,
) {
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val newProfilePic = "${UUID.randomUUID()}.${getFileExtension(context, uri)}"
            val copyResult = copyUriToInternalStorage(
                context = context,
                uri = uri,
                fileName = "profile-pictures/${newProfilePic}"
            )
            if (copyResult == null) {
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
                name = name,
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
    description: String,
    onDescriptionChanged: (String) -> Unit,
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
                value = description,
                onValueChange = onDescriptionChanged,
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
    name: String,
    description: String,
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
                    name = name,
                    modifier = Modifier.size(114.dp)
                )
                Column {
                    Text(
                        text = name.ifEmpty { "(Empty)" },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = description.ifEmpty { "(Empty)" },
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
