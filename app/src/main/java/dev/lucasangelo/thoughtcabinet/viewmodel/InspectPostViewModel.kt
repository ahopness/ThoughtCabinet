package dev.lucasangelo.thoughtcabinet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.lucasangelo.thoughtcabinet.data.AppRepository
import dev.lucasangelo.thoughtcabinet.data.CommentEntity
import kotlinx.coroutines.launch

class InspectPostViewModel (
    private val repository: AppRepository,
    application: Application
) : AndroidViewModel(application) {
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