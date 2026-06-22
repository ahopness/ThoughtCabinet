package dev.lucasangelo.thoughtcabinet.ui.screen.edit

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.lucasangelo.thoughtcabinet.data.AppDao
import dev.lucasangelo.thoughtcabinet.data.PersonaEntity
import dev.lucasangelo.thoughtcabinet.util.cleanupDrafts
import dev.lucasangelo.thoughtcabinet.util.copyInInternalStorage
import dev.lucasangelo.thoughtcabinet.util.copyUriToInternalStorage
import dev.lucasangelo.thoughtcabinet.util.draftsDir
import dev.lucasangelo.thoughtcabinet.util.getFileExtension
import dev.lucasangelo.thoughtcabinet.util.profilePicDir
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.UUID

class EditPersonaViewModel(
    private val dao: AppDao,
    application: Application,
) : AndroidViewModel(application) {
    // NOTE: using Two-Way Data Binding here for simplicity’s sake
    // might change in the future, I just don't wanna write a bunch of setters for such a simple screen rn
    var personaName by mutableStateOf("")
    var personaBio by mutableStateOf("")
    var personaProfilePic by mutableStateOf<String?>(null)
    var personaColorTheme by mutableStateOf(Color.Black)

    var hasLoadedPersona by mutableStateOf(false)
        private set
    var persona by mutableStateOf<PersonaEntity?>(null)
        private set
    fun fetchPersona(id: Long?) {
        viewModelScope.launch {
            persona = if (id != null) dao.getPersona(id) else null
            persona?.let {
                personaName = it.name
                personaBio = it.bio
                personaProfilePic = it.profilePic
                personaColorTheme = Color(it.colorTheme)
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
                name = personaName.trim(),
                bio = personaBio.trim(),
                profilePic = personaProfilePic,
                colorTheme = personaColorTheme.toArgb(),
                traits = from.traits,
                metadata = from.metadata,
            ))
        }
    }
    fun insertPersona() {
        viewModelScope.launch {
            dao.insertPersona(PersonaEntity(
                createdAt = Instant.now(),
                updatedAt = null,
                name = personaName.trim(),
                bio = personaBio.trim(),
                profilePic = personaProfilePic,
                colorTheme = personaColorTheme.toArgb(),
                traits = emptyList(),
                metadata = emptyMap(),
            ))
        }
    }

    var hasNewProfilePicDraft by mutableStateOf(false)
        private set
    fun importProfilePic(uri: Uri) : String? {
        val context = getApplication<Application>()

        val newProfilePic = "${UUID.randomUUID()}.${getFileExtension(context, uri)}"
        val copyResult = copyUriToInternalStorage(
            context = context,
            uri = uri,
            fileParentDir = context.cacheDir,
            fileName = draftsDir + newProfilePic
        )
        if (copyResult != null) {
            hasNewProfilePicDraft = true
            return newProfilePic
        } else {
            return null
        }
    }
    suspend fun commitProfilePic() : Boolean = withContext(Dispatchers.IO) {
        if (personaProfilePic.isNullOrEmpty() || !hasNewProfilePicDraft)
            return@withContext true

        val context = getApplication<Application>()
        val commitedProfilePic = copyInInternalStorage(
            context.cacheDir, draftsDir + personaProfilePic,
            context.filesDir, profilePicDir + personaProfilePic
        )

        commitedProfilePic != null
    }
    fun cleanupProfilePicDrafts() =
        cleanupDrafts(getApplication<Application>())
}