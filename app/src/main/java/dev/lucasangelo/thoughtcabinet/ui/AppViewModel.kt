package dev.lucasangelo.thoughtcabinet.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.lucasangelo.thoughtcabinet.data.AppDao
import dev.lucasangelo.thoughtcabinet.data.PersonaEntity
import dev.lucasangelo.thoughtcabinet.data.PostEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class AppViewModel(private val dao: AppDao) : ViewModel() {
    val thoughts: StateFlow<List<PostEntity>> = dao.getAllPosts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // NOTE: standard android boilerplate for smooth rotations
            initialValue = emptyList()
        )

    val crowd: StateFlow<List<PersonaEntity>> = dao.getAllPersonas()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}