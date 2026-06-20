package dev.lucasangelo.thoughtcabinet.ui.screen.edit

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.lucasangelo.thoughtcabinet.data.AppDao
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
import java.time.Instant
import java.util.UUID
import kotlin.let

class EditPersonaTraitViewModel(
    private val dao: AppDao,
    application: Application
) : AndroidViewModel(application) {
    // NOTE: using Two-Way Data Binding here for simplicity’s sake
    // might change in the future, I just don't wanna write a bunch of setters for such a simple screen rn
    var colorTheme by mutableStateOf(Color.Black)
    var traitType by mutableStateOf(PersonaTraitType.TEXT)
    var traitContent by mutableStateOf("")

    var hasLoadedPersona by mutableStateOf(false)
        private set
    var persona by mutableStateOf<PersonaEntity?>(null)
        private set
    var hasLoadedTrait by mutableStateOf(false)
        private set
    var trait by mutableStateOf<PersonaTrait?>(null)
        private set
    fun fetchPersonaAndTrait(personaId: Long, traitId: Int?) {
        viewModelScope.launch {
            persona = dao.getPersona(personaId)

            persona?.let { currentPersona ->
                colorTheme = Color(currentPersona.colorTheme)

                if (traitId != null)
                    trait = currentPersona.traits[traitId]

                trait?.let { currentTrait ->
                    traitType = currentTrait.type
                    traitContent = currentTrait.content
                }
            }
            hasLoadedPersona = true
            hasLoadedTrait = true
        }
    }

    fun updateTrait(from: PersonaTrait, at: PersonaEntity) {
        viewModelScope.launch {
            val newTraits = at.traits.map { trait ->
                if (trait == from) {
                    PersonaTrait(
                        type = traitType,
                        content = traitContent,
                        metadata = from.metadata
                    )
                } else {
                    trait
                }
            }
            val updatedPersona = at.copy(
                traits = newTraits,
                updatedAt = Instant.now()
            )
            dao.updatePersona(updatedPersona)
        }
    }
    fun insertTrait(at: PersonaEntity) {
        viewModelScope.launch {
            val newTrait = PersonaTrait(
                type = traitType,
                content = traitContent,
                metadata = emptyMap()
            )
            val newTraits = at.traits + newTrait
            val updatedPersona = at.copy(
                traits = newTraits,
                updatedAt = Instant.now()
            )
            dao.updatePersona(updatedPersona)
        }
    }

    var hasMediaDraft by mutableStateOf(false)
        private set
    fun importMedia(uri: Uri) : String? {
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
            return newMedia
        } else {
            return null
        }
    }
    suspend fun commitMedia() : Boolean = withContext(Dispatchers.IO) {
        if (traitContent.isEmpty() || !hasMediaDraft)
            return@withContext true

        val context = getApplication<Application>()
        var commitedMedia = copyInInternalStorage(
            context.cacheDir, draftsDir + traitContent,
            context.filesDir, traitsDir + traitContent
        )

        commitedMedia != null
    }
    fun cleanupMediaDrafts() =
        cleanupDrafts(getApplication<Application>())
}