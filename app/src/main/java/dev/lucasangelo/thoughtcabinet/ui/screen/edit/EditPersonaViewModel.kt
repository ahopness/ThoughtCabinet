package dev.lucasangelo.thoughtcabinet.ui.screen.edit

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.lucasangelo.thoughtcabinet.data.AppDao
import dev.lucasangelo.thoughtcabinet.data.PersonaEntity
import dev.lucasangelo.thoughtcabinet.util.cleanupDrafts
import dev.lucasangelo.thoughtcabinet.util.copyInInternalStorage
import dev.lucasangelo.thoughtcabinet.util.copyUriToInternalStorage
import dev.lucasangelo.thoughtcabinet.util.draftsDir
import dev.lucasangelo.thoughtcabinet.util.getFileExtension
import dev.lucasangelo.thoughtcabinet.util.profilePicDir
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID

class EditPersonaViewModel(private val dao: AppDao) : ViewModel() {
    // NOTE: using Two-Way Data Binding here for simplicity’s sake
    // might change in the future, I just don't wanna write a bunch of setters for such a simple screen rn
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
    fun insertPersona() {
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

    var hasNewProfilePicDraft by mutableStateOf(false)
        private set
    fun importProfilePic(
        context: Context,
        uri: Uri,
    ) : String? {
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
    fun commitProfilePic(
        context: Context,
    ) : Boolean {
        if (profilePic == null || !hasNewProfilePicDraft)
            return true

        var commitedProfilePic = copyInInternalStorage(
            context.cacheDir, draftsDir + profilePic,
            context.filesDir, profilePicDir + profilePic
        )

        return commitedProfilePic != null
    }
    fun cleanupProfilePicDrafts(context: Context) =
        cleanupDrafts(context)
}