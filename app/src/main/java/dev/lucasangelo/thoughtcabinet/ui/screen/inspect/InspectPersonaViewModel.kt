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
import dev.lucasangelo.thoughtcabinet.data.AppRepository
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
    private val repository: AppRepository,
    application: Application
) : AndroidViewModel(application) {
    var hasLoadedPersona by mutableStateOf(false)
        private set
    var persona by mutableStateOf<PersonaEntity?>(null)
        private set
    fun fetchPersona(id: Long) {
        viewModelScope.launch {
            persona = repository.getPersona(id)
            hasLoadedPersona = true
        }
    }

    suspend fun deletePersona(persona: PersonaEntity) = repository.deletePersona(persona)

    suspend fun deleteTrait(trait: PersonaTrait, at: PersonaEntity) {
        persona = repository.deleteTrait(trait, at)
    }
    fun moveTrait(fromIndex: Int, toIndex: Int, at: PersonaEntity) {
        viewModelScope.launch {
            val updatedPersona = repository.moveTrait(fromIndex, toIndex, at)
            updatedPersona?.let { persona = updatedPersona }
        }
    }
}