package dev.lucasangelo.thoughtcabinet.ui.screen.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dev.lucasangelo.thoughtcabinet.data.AppRepository
import dev.lucasangelo.thoughtcabinet.data.PostEntity

class ThoughtsViewModel(
    private val repository: AppRepository,
    application: Application
) : AndroidViewModel(application) {
    suspend fun deletePost(post: PostEntity) =
        repository.deletePost(post)
    suspend fun likePost(post: PostEntity) =
        repository.likePost(post)
}