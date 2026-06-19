package dev.lucasangelo.thoughtcabinet.ui.screen.edit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.lucasangelo.thoughtcabinet.data.AppDao
import dev.lucasangelo.thoughtcabinet.data.PersonaEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant

class EditPersonaViewModel(private val dao: AppDao) : ViewModel() {
    // NOTE: using Two-Way Data Binding here for simplicity’s sake
    // might change in the future, I just don't wanna write 10 setters for such a simple screen rn
    var nameText by mutableStateOf("")
    var bioText by mutableStateOf("")
    var profilePic by mutableStateOf<String?>(null)
    var colorTheme by mutableStateOf(Color.Black)

    var hasLoadedPersona by mutableStateOf(false)
        private set
    var persona by mutableStateOf<PersonaEntity?>(null)
        private set

    fun fetchPersona(id: Long?) {
        viewModelScope.launch {
            persona = if (id != null) dao.getPersona(id) else null
            persona?.let {
                nameText = it.name
                bioText = it.bio
                profilePic = it.profilePic
                colorTheme = Color(it.colorTheme)
            }
            hasLoadedPersona = true
        }
    }

    fun updatePersona(from: PersonaEntity) {
        viewModelScope.launch {
            dao.updatePersona(PersonaEntity(
                id = from.id,
                createdAt = from.createdAt,
                updatedAt = Instant.now(),
                name = nameText.trim(),
                bio = bioText.trim(),
                profilePic = profilePic,
                colorTheme = colorTheme.toArgb(),
                traits = from.traits,
                metadata = from.metadata,
            ))
        }
    }
    fun InsertPersona() {
        viewModelScope.launch {
            dao.insertPersona(PersonaEntity(
                id = 0,
                createdAt = Instant.now(),
                updatedAt = null,
                name = nameText.trim(),
                bio = bioText.trim(),
                profilePic = profilePic,
                colorTheme = colorTheme.toArgb(),
                traits = emptyList(),
                metadata = emptyMap(),
            ))
        }
    }

    // NOTE: the 'Clear Current' button only sets the variable to null
    // because if the user exists this screen without saving after deleting a pfp
    // the path the variable is pointing to gets invalidated
    fun cleanupProfilePics(filesDir: File) {
        viewModelScope.launch(Dispatchers.IO) {
            val referencedProfilePics = dao.getAllPersonas()
                .first()
                .mapNotNull { it.profilePic }
                .toSet()

            val folder = File(filesDir, "profile-pictures")
            folder.listFiles()
                ?.filter { it.isFile && it.name !in referencedProfilePics }
                ?.forEach { it.delete() }
        }
    }
}