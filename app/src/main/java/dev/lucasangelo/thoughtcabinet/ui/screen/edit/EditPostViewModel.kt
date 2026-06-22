package dev.lucasangelo.thoughtcabinet.ui.screen.edit

import android.app.Application
import android.net.Uri
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
import dev.lucasangelo.thoughtcabinet.util.cleanupDrafts
import dev.lucasangelo.thoughtcabinet.util.copyInInternalStorage
import dev.lucasangelo.thoughtcabinet.util.copyUriToInternalStorage
import dev.lucasangelo.thoughtcabinet.util.draftsDir
import dev.lucasangelo.thoughtcabinet.util.getFileExtension
import dev.lucasangelo.thoughtcabinet.util.mediaDir
import dev.lucasangelo.thoughtcabinet.util.traitsDir
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.UUID

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

    var hasMediaDraft by mutableStateOf(false)
        private set
    fun importMedia(uri: Uri) : String? {
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
            return newMedia
        } else {
            return null
        }
    }
    suspend fun commitMedia() : Boolean = withContext(Dispatchers.IO) {
        if (postMedia.isEmpty() || !hasMediaDraft)
            return@withContext true

        val context = getApplication<Application>()
        postMedia.forEach { media ->
            var commitedMedia = copyInInternalStorage(
                context.cacheDir, draftsDir + media,
                context.filesDir, mediaDir + media
            )

            if (commitedMedia == null)
                return@withContext false
        }

        return@withContext true
    }
    fun cleanupMediaDrafts() =
        cleanupDrafts(getApplication<Application>())

    suspend fun updatePost(from: PostEntity) {
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
    suspend fun insertPost(asRepostOf: Long?, isArchived: Boolean) {
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