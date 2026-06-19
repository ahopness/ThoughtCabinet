package dev.lucasangelo.thoughtcabinet.ui.screen.inspect

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.lucasangelo.thoughtcabinet.data.AppDao
import dev.lucasangelo.thoughtcabinet.data.PersonaEntity
import kotlinx.coroutines.launch

class InspectPersonaViewModel(private val dao: AppDao) : ViewModel() {
    var persona by mutableStateOf<PersonaEntity?>(null)
        private set

    fun fetchPersona(id: Long) {
        viewModelScope.launch {
            persona = dao.getPersona(id)
        }
    }

    fun deletePersona(persona: PersonaEntity) {
        viewModelScope.launch {
            dao.deletePersona(persona)
        }
    }
}