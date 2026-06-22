package dev.lucasangelo.thoughtcabinet.ui.screen.edit

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.lucasangelo.thoughtcabinet.data.AppDao
import dev.lucasangelo.thoughtcabinet.data.PersonaEntity
import dev.lucasangelo.thoughtcabinet.data.PostEntity
import dev.lucasangelo.thoughtcabinet.data.PostType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant

class EditPostViewModel(
    private val dao: AppDao,
    application: Application
) : AndroidViewModel(application) {
    // NOTE: using Two-Way Data Binding here for simplicity’s sake
    // might change in the future, I just don't wanna write a bunch of setters for such a simple screen rn
    var postIsRepostOf by mutableStateOf<Long?>(null)
    var postType by mutableStateOf<PostType>(PostType.NOTE)
    var postContent by mutableStateOf("")
    var postMedia by mutableStateOf<List<String>>(emptyList())
    var postMood by mutableStateOf("")

    // NOTE: can cause a race condition, might replace this if it ever causes too much trouble
    var personaColorTheme by mutableStateOf(Color.Black)
        private set
    private var _postAuthorId by mutableStateOf<Long?>(null)
    var postAuthorId: Long?
        get() = _postAuthorId
        set(value) {
            if (value == null) {
                personaColorTheme = Color.Black
            } else {
                viewModelScope.launch {
                    dao.getPersona(value)?.let {
                        personaColorTheme = Color(it.colorTheme)
                    }
                }
            }

            _postAuthorId = value
        }

    var hasLoadedPost by mutableStateOf(false)
        private set
    var post by mutableStateOf<PostEntity?>(null)
        private set
    fun fetchPost(id: Long?, repostOf: Long?) {
        viewModelScope.launch {
            post = if (id != null) dao.getPost(id) else null
            post?.let {
                postAuthorId = it.authorId
                postType = it.type
                postContent = it.content
                postMedia = it.media
                postMood = it.mood
            }

            postIsRepostOf = repostOf

            hasLoadedPost = true
        }
    }

    fun updatePost(from: PostEntity) {
        viewModelScope.launch {
            dao.updatePost(PostEntity(
                id = from.id,
                repostOf = from.repostOf,
                createdAt = from.createdAt,
                updatedAt = Instant.now(),
                authorId = postAuthorId ?: 0L,
                type = postType,
                content = postContent.trim(),
                media = postMedia,
                mood = postMood.trim(),
                liked = from.liked,
                bookmarked = from.bookmarked,
                archived = from.archived,
                metadata = from.metadata
            ))
        }
    }
    fun insertPost(asRepostOf: Long?, isArchived: Boolean) {
        viewModelScope.launch {
            dao.updatePost(PostEntity(
                repostOf = asRepostOf,
                createdAt = Instant.now(),
                updatedAt = null,
                authorId = postAuthorId!!,
                type = postType,
                content = postContent.trim(),
                media = postMedia,
                mood = postMood.trim(),
                liked = false,
                bookmarked = false,
                archived = isArchived,
                metadata = emptyMap()
            ))
        }
    }
}