package dev.lucasangelo.thoughtcabinet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.lucasangelo.thoughtcabinet.data.AppRepository
import dev.lucasangelo.thoughtcabinet.data.PersonaEntity
import dev.lucasangelo.thoughtcabinet.data.PostEntity
import kotlinx.coroutines.launch

class PostViewModel(
    private val repository: AppRepository
) : ViewModel() {
    fun deletePost(post: PostEntity) = viewModelScope.launch {
        repository.deletePost(post)
    }
    fun likePost(post: PostEntity) = viewModelScope.launch {
        repository.likePost(post)
    }
    fun bookmarkPost(post: PostEntity) = viewModelScope.launch {
        repository.bookmarkPost(post)
    }
    fun blockPersona(persona: PersonaEntity) = viewModelScope.launch {
        repository.blockPersona(persona)
    }

}