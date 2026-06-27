package dev.lucasangelo.thoughtcabinet.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.lucasangelo.thoughtcabinet.data.AppRepository
import dev.lucasangelo.thoughtcabinet.data.PostEntity
import dev.lucasangelo.thoughtcabinet.data.PostType
import dev.lucasangelo.thoughtcabinet.util.cleanupDrafts
import dev.lucasangelo.thoughtcabinet.util.copyInInternalStorage
import dev.lucasangelo.thoughtcabinet.util.copyUriToInternalStorage
import dev.lucasangelo.thoughtcabinet.util.draftsDir
import dev.lucasangelo.thoughtcabinet.util.getFileExtension
import dev.lucasangelo.thoughtcabinet.util.mediaDir
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class EditPostViewModel(
    private val repository: AppRepository,
    application: Application
) : AndroidViewModel(application) {
    // NOTE: using Two-Way Data Binding here for simplicity’s sake
    // might change in the future, I just don't wanna write a bunch of setters for such a simple screen rn
    var postIsRepostOf by mutableStateOf<Long?>(null)
    var postType by mutableStateOf(PostType.NOTE)
    var postContent by mutableStateOf("")
    var postMedia by mutableStateOf<List<String>>(emptyList())

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
                    repository.getPersona(value)?.let {
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
            post = if (id != null) repository.getPost(id) else null
            post?.let {
                postAuthorId = it.authorId
                postType = it.type
                postContent = it.content
                postMedia = it.media
            }

            postIsRepostOf = repostOf

            hasLoadedPost = true
        }
    }

    var hasMediaDraft by mutableStateOf(false)
        private set
    suspend fun importMedia(uri: Uri) : String? = withContext(Dispatchers.IO) {
        val context = getApplication<Application>()

        val newMedia = "${UUID.randomUUID()}.${getFileExtension(context, uri)}"
        val copyResult = copyUriToInternalStorage(
            context = context,
            uri = uri,
            fileParentDir = context.cacheDir,
            fileName = draftsDir + newMedia
        )
        if (copyResult != null) {
            hasMediaDraft = true
            return@withContext newMedia
        } else {
            return@withContext null
        }
    }
    suspend fun commitMedia() = withContext(Dispatchers.IO) {
        if (postMedia.isEmpty() || !hasMediaDraft)
            return@withContext

        val context = getApplication<Application>()
        postMedia.forEach { media ->
            copyInInternalStorage(
                context.cacheDir, draftsDir + media,
                context.filesDir, mediaDir + media
            )
        }
    }
    fun cleanupMediaDrafts() = viewModelScope.launch {
        cleanupDrafts(getApplication<Application>())
    }

    fun updatePost(from: PostEntity, isArchived: Boolean) = viewModelScope.launch {
        repository.updatePost(
            from,
            isArchived,
            postAuthorId!!,
            postType,
            postContent,
            postMedia,
        )
    }
    fun insertPost(asRepostOf: Long?, isArchived: Boolean) = viewModelScope.launch {
        repository.insertPost(
            asRepostOf,
            isArchived,
            postAuthorId!!,
            postType,
            postContent,
            postMedia,
        )
    }
}