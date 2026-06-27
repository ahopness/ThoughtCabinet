package dev.lucasangelo.thoughtcabinet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.lucasangelo.thoughtcabinet.data.AppRepository
import dev.lucasangelo.thoughtcabinet.data.CommentEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InspectPostViewModel (
    private val repository: AppRepository,
    application: Application,
    val postId: Long
) : AndroidViewModel(application) {
    val comments: StateFlow<List<CommentEntity>> = repository.getAllCommentsOf(postId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun insertComment(atPost: Long, ofAuthor: Long, content: String) = viewModelScope.launch {
        repository.insertComment(atPost, ofAuthor, content)
    }
    fun updateComment(from: CommentEntity, ofAuthor: Long, content: String) = viewModelScope.launch {
        repository.updateComment(from, ofAuthor, content)
    }
    fun deleteComment(comment: CommentEntity) = viewModelScope.launch {
        repository.deleteComment(comment)
    }
}