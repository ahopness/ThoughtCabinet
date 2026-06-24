package dev.lucasangelo.thoughtcabinet.ui.screen.edit

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.lucasangelo.thoughtcabinet.data.AppRepository
import dev.lucasangelo.thoughtcabinet.data.PersonaEntity
import dev.lucasangelo.thoughtcabinet.data.PersonaTrait
import dev.lucasangelo.thoughtcabinet.data.PersonaTraitType
import dev.lucasangelo.thoughtcabinet.util.cleanupDrafts
import dev.lucasangelo.thoughtcabinet.util.copyInInternalStorage
import dev.lucasangelo.thoughtcabinet.util.copyUriToInternalStorage
import dev.lucasangelo.thoughtcabinet.util.draftsDir
import dev.lucasangelo.thoughtcabinet.util.getFileExtension
import dev.lucasangelo.thoughtcabinet.util.traitsDir
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class EditPersonaTraitViewModel(
    private val repository: AppRepository,
    application: Application
) : AndroidViewModel(application) {
    // NOTE: using Two-Way Data Binding here for simplicity’s sake
    // might change in the future, I just don't wanna write a bunch of setters for such a simple screen rn
    var traitType by mutableStateOf(PersonaTraitType.TEXT)
    var traitContent by mutableStateOf("")

    var personaColorTheme by mutableStateOf(Color.Black)
        private set

    var persona by mutableStateOf<PersonaEntity?>(null)
        private set

    var hasLoadedTrait by mutableStateOf(false)
        private set
    var trait by mutableStateOf<PersonaTrait?>(null)
        private set
    fun fetchPersonaAndTrait(personaId: Long, traitId: Int?) {
        viewModelScope.launch {
            persona = repository.getPersona(personaId)

            persona?.let { currentPersona ->
                personaColorTheme = Color(currentPersona.colorTheme)

                if (traitId != null)
                    trait = repository.getTrait(currentPersona, traitId)

                trait?.let { currentTrait ->
                    traitType = currentTrait.type
                    traitContent = currentTrait.content
                }
            }
            hasLoadedTrait = true
        }
    }

    var hasMediaDraft by mutableStateOf(false)
        private set
    suspend fun importMedia(uri: Uri) : String? = withContext(Dispatchers.IO) {
        val context = getApplication<Application>()

        val newMedia = "${UUID.randomUUID()}.${getFileExtension(context, uri)}"
        val copyResult = copyUriToInternalStorage(
            context = context,
            uri = uri,
            fileParentDir = context.cacheDir,
            fileName = draftsDir + newMedia
        )
        if (copyResult != null) {
            hasMediaDraft = true
            return@withContext newMedia
        } else {
            return@withContext null
        }
    }
    suspend fun commitMedia() = withContext(Dispatchers.IO) {
        if (traitContent.isEmpty() || !hasMediaDraft)
            return@withContext

        val context = getApplication<Application>()
        copyInInternalStorage(
            context.cacheDir, draftsDir + traitContent,
            context.filesDir, traitsDir + traitContent
        )
    }
    fun cleanupMediaDrafts() = viewModelScope.launch {
        cleanupDrafts(getApplication<Application>())
    }

    fun updateTrait(from: PersonaTrait, at: PersonaEntity) = viewModelScope.launch {
        repository.updateTrait(from, at, traitType, traitContent)
    }
    fun insertTrait(at: PersonaEntity) = viewModelScope.launch {
        repository.insertTrait(at, traitType, traitContent)
    }

}