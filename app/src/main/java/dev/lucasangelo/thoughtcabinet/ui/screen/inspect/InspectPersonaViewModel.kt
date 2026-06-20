package dev.lucasangelo.thoughtcabinet.ui.screen.inspect

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.lucasangelo.thoughtcabinet.data.AppDao
import dev.lucasangelo.thoughtcabinet.data.PersonaEntity
import dev.lucasangelo.thoughtcabinet.data.PersonaTrait
import dev.lucasangelo.thoughtcabinet.data.PersonaTraitType
import dev.lucasangelo.thoughtcabinet.util.deleteInternalStorageFile
import dev.lucasangelo.thoughtcabinet.util.traitsDir
import kotlinx.coroutines.launch
import java.time.Instant

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

    fun deleteTrait(context: Context, trait: PersonaTrait, at: PersonaEntity) {
        viewModelScope.launch {
            if (trait.type == PersonaTraitType.MEDIA) {
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