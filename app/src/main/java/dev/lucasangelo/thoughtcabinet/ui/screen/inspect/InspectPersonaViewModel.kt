package dev.lucasangelo.thoughtcabinet.ui.screen.inspect

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.lucasangelo.thoughtcabinet.data.AppDao
import dev.lucasangelo.thoughtcabinet.data.PersonaEntity
import dev.lucasangelo.thoughtcabinet.data.PersonaTrait
import dev.lucasangelo.thoughtcabinet.data.PersonaTraitType
import dev.lucasangelo.thoughtcabinet.util.LinkMetadata
import dev.lucasangelo.thoughtcabinet.util.deleteInternalStorageFile
import dev.lucasangelo.thoughtcabinet.util.fetchLinkMetadata
import dev.lucasangelo.thoughtcabinet.util.mediaDir
import dev.lucasangelo.thoughtcabinet.util.traitsDir
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant

class InspectPersonaViewModel(
    private val dao: AppDao,
    application: Application
) : AndroidViewModel(application) {
    var hasLoadedPersona by mutableStateOf(false)
        private set
    var persona by mutableStateOf<PersonaEntity?>(null)
        private set
    fun fetchPersona(id: Long) {
        viewModelScope.launch {
            persona = dao.getPersona(id)
            hasLoadedPersona = true
        }
    }

    suspend fun deletePersona(persona: PersonaEntity) = withContext(Dispatchers.IO) {
        val context = getApplication<Application>()

        persona.traits.forEach {
            if (it.type == PersonaTraitType.MEDIA)
                deleteInternalStorageFile(context.filesDir, traitsDir + it.content)
        }

        val posts = dao.getAllPostsBy(persona.id).first()
        posts.forEach { post ->
            post.media.forEach { media ->
                deleteInternalStorageFile(context.filesDir, mediaDir + media)
            }
        }

        dao.deletePersona(persona)
    }

    suspend fun deleteTrait(trait: PersonaTrait, at: PersonaEntity) = withContext(Dispatchers.IO) {
        if (trait.type == PersonaTraitType.MEDIA) {
            val context = getApplication<Application>()
            deleteInternalStorageFile(context.filesDir, traitsDir + trait.content)
        }

        val newTraits = at.traits - trait

        val updatedPersona = at.copy(
            traits = newTraits,
            updatedAt = Instant.now()
        )
        dao.updatePersona(updatedPersona)

        persona = updatedPersona
    }
    fun moveTrait(fromIndex: Int, toIndex: Int, at: PersonaEntity) {
        if (fromIndex !in at.traits.indices || toIndex !in at.traits.indices) return
        if (fromIndex == toIndex) return

        viewModelScope.launch {
            val newTraits = at.traits.toMutableList().apply {
                val trait = removeAt(fromIndex)
                add(toIndex, trait)
            }

            val updatedPersona = at.copy(
                traits = newTraits,
                updatedAt = Instant.now()
            )

            dao.updatePersona(updatedPersona)

            persona = updatedPersona
        }
    }
}