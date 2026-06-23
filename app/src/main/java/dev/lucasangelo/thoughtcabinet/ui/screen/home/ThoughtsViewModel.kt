package dev.lucasangelo.thoughtcabinet.ui.screen.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dev.lucasangelo.thoughtcabinet.data.AppDao
import dev.lucasangelo.thoughtcabinet.data.PostEntity
import dev.lucasangelo.thoughtcabinet.util.deleteInternalStorageFile
import dev.lucasangelo.thoughtcabinet.util.mediaDir
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ThoughtsViewModel(
    private val dao: AppDao,
    application: Application
) : AndroidViewModel(application) {
    suspend fun deletePost(post: PostEntity) = withContext(Dispatchers.IO) {
        post.media.forEach {
            val context = getApplication<Application>()
            deleteInternalStorageFile(context.filesDir, mediaDir + it)
        }

        dao.deletePost(post)
    }
    suspend fun likePost(post: PostEntity) {
        dao.updatePost(post.copy(liked = !post.liked))
    }
}