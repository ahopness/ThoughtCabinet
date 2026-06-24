package dev.lucasangelo.thoughtcabinet.ui.screen.inspect

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.lucasangelo.thoughtcabinet.data.AppRepository
import dev.lucasangelo.thoughtcabinet.data.CommentEntity
import kotlinx.coroutines.launch

class InspectPostViewModel (
    private val repository: AppRepository,
    application: Application
) : AndroidViewModel(application) {
    var hasLoadedComments by mutableStateOf(false)
        private set
    var comments = mutableStateListOf<CommentEntity>()
    fun fetchComments(postId: Long) = viewModelScope.launch {
        repository.getAllCommentsOf(postId).collect { list ->
            comments.clear()
            comments.addAll(list)
            hasLoadedComments = true
        }
    }

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