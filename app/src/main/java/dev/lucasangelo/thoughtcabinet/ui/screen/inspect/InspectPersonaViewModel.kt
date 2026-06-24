package dev.lucasangelo.thoughtcabinet.ui.screen.inspect

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.lucasangelo.thoughtcabinet.data.AppRepository
import dev.lucasangelo.thoughtcabinet.data.PersonaEntity
import dev.lucasangelo.thoughtcabinet.data.PersonaTrait
import kotlinx.coroutines.launch

class InspectPersonaViewModel(
    private val repository: AppRepository,
    application: Application
) : AndroidViewModel(application) {
    var hasLoadedPersona by mutableStateOf(false)
        private set
    var persona by mutableStateOf<PersonaEntity?>(null)
        private set
    fun fetchPersona(id: Long) = viewModelScope.launch {
        persona = repository.getPersona(id)
        hasLoadedPersona = true
    }

    fun deletePersona(persona: PersonaEntity) = viewModelScope.launch {
        repository.deletePersona(persona)
    }

    fun deleteTrait(trait: PersonaTrait, at: PersonaEntity) = viewModelScope.launch {
        persona = repository.deleteTrait(trait, at)
    }
    fun moveTrait(fromIndex: Int, toIndex: Int, at: PersonaEntity) = viewModelScope.launch {
        val updatedPersona = repository.moveTrait(fromIndex, toIndex, at)
        updatedPersona?.let { persona = updatedPersona }
    }
}