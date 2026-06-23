package dev.lucasangelo.thoughtcabinet.ui.screen.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.lucasangelo.thoughtcabinet.data.AppRepository
import dev.lucasangelo.thoughtcabinet.data.PostEntity
import kotlinx.coroutines.launch

class ThoughtsViewModel(
    private val repository: AppRepository,
    application: Application
) : AndroidViewModel(application) {
    fun deletePost(post: PostEntity) = viewModelScope.launch {
        repository.deletePost(post)
    }
    fun likePost(post: PostEntity) = viewModelScope.launch {
        repository.likePost(post)
    }
    fun bookmarkPost(post: PostEntity) = viewModelScope.launch {
        repository.bookmarkPost(post)
    }
}