package dev.lucasangelo.thoughtcabinet.data

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import dev.lucasangelo.thoughtcabinet.util.deleteInternalStorageFile
import dev.lucasangelo.thoughtcabinet.util.mediaDir
import dev.lucasangelo.thoughtcabinet.util.traitsDir
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.Instant

class AppRepository(
    private val dao: AppDao,
    private val context: Context
) {
    //region PERSONA
    suspend fun insertPersona(
        personaName: String,
        personaBio: String,
        personaProfilePic: String?,
        personaColorTheme: Color
    ) {
        dao.insertPersona(PersonaEntity(
            createdAt = Instant.now(),
            updatedAt = null,
            name = personaName.trim(),
            bio = personaBio.trim(),
            profilePic = personaProfilePic,
            colorTheme = personaColorTheme.toArgb(),
            traits = emptyList(),
            blocked = false,
            metadata = emptyMap(),
        ))
    }
    suspend fun getPersona(id: Long) = dao.getPersona(id)
    fun getAllPersonas() = dao.getAllPersonas()
    suspend fun deletePersona(persona: PersonaEntity) = withContext(Dispatchers.IO) {
        persona.traits.forEach {
            if (it.type == PersonaTraitType.MEDIA)
                deleteInternalStorageFile(context.filesDir, traitsDir + it.content)
        }

        val posts = dao.getAllPostsBy(persona.id).first()
        posts.forEach { post ->
            post.media.forEach { media ->
                deleteInternalStorageFile(context.filesDir, mediaDir + media)
            }
        }

        dao.deletePersona(persona)
    }
    suspend fun updatePersona(
        from: PersonaEntity,
        personaName: String,
        personaBio: String,
        personaProfilePic: String?,
        personaColorTheme: Color
    ) {
        dao.updatePersona(from.copy(
            updatedAt = Instant.now(),
            name = personaName.trim(),
            bio = personaBio.trim(),
            profilePic = personaProfilePic,
            colorTheme = personaColorTheme.toArgb(),
        ))
    }
    suspend fun blockPersona(persona: PersonaEntity) {
        dao.updatePersona(persona.copy(blocked = !persona.blocked))
    }
    //endregion

    //region TRAITS
    suspend fun insertTrait(
        at: PersonaEntity,
        traitType: PersonaTraitType,
        traitContent: String
    ) {
        val newTrait = PersonaTrait(
            type = traitType,
            content = traitContent,
            metadata = emptyMap()
        )
        val newTraits = at.traits + newTrait
        val updatedPersona = at.copy(
            traits = newTraits,
            updatedAt = Instant.now()
        )
        dao.updatePersona(updatedPersona)
    }
    fun getTrait(at: PersonaEntity, withId: Int) = at.traits[withId]
    suspend fun deleteTrait(trait: PersonaTrait, at: PersonaEntity) : PersonaEntity = withContext(Dispatchers.IO) {
        if (trait.type == PersonaTraitType.MEDIA) {
            deleteInternalStorageFile(context.filesDir, traitsDir + trait.content)
        }

        val newTraits = at.traits - trait

        val updatedPersona = at.copy(
            traits = newTraits,
            updatedAt = Instant.now()
        )
        dao.updatePersona(updatedPersona)

        return@withContext updatedPersona
    }
    suspend fun updateTrait(
        from: PersonaTrait,
        at: PersonaEntity,
        traitType: PersonaTraitType,
        traitContent: String
    ) {
        val newTraits = at.traits.map { trait ->
            if (trait == from) {
                PersonaTrait(
                    type = traitType,
                    content = traitContent,
                    metadata = from.metadata
                )
            } else {
                trait
            }
        }
        val updatedPersona = at.copy(
            traits = newTraits,
            updatedAt = Instant.now()
        )
        dao.updatePersona(updatedPersona)
    }
    suspend fun moveTrait(fromIndex: Int, toIndex: Int, at: PersonaEntity) : PersonaEntity? {
        if (fromIndex !in at.traits.indices || toIndex !in at.traits.indices) return null
        if (fromIndex == toIndex) return null

        val newTraits = at.traits.toMutableList().apply {
            val trait = removeAt(fromIndex)
            add(toIndex, trait)
        }

        val updatedPersona = at.copy(
            traits = newTraits,
            updatedAt = Instant.now()
        )

        dao.updatePersona(updatedPersona)

        return updatedPersona
    }
    //endregion

    //region POSTS
    suspend fun insertPost(
        asRepostOf: Long?,
        isArchived: Boolean,
        postAuthorId: Long,
        postType: PostType,
        postContent: String,
        postMedia: List<String>,
    ) {
        dao.insertPost(PostEntity(
            repostOf = asRepostOf,
            createdAt = Instant.now(),
            updatedAt = null,
            authorId = postAuthorId,
            type = postType,
            content = postContent.trim(),
            media = postMedia,
            liked = false,
            bookmarked = false,
            archived = isArchived,
            metadata = emptyMap()
        ))
    }
    suspend fun getPost(id: Long) = dao.getPost(id)
    fun getAllPosts() = dao.getAllPosts()
    fun getAllValidPosts() = dao.getAllValidPosts()
    fun getAllBookmarkedPosts() = dao.getAllBookmarkedPosts()
    fun getAllArchivedPosts() = dao.getAllArchivedPosts()
    fun getAllPostsFromBlockedPersonas() = dao.getAllPostsFromBlockedPersonas()
    fun getAllPostsBy(authorId: Long) = dao.getAllPostsBy(authorId)
    fun searchPosts(query: String) = dao.searchPosts(query)
    suspend fun deletePost(post: PostEntity) = withContext(Dispatchers.IO) {
        post.media.forEach {
            deleteInternalStorageFile(context.filesDir, mediaDir + it)
        }

        dao.deletePost(post)
    }
    suspend fun updatePost(
        from: PostEntity,
        isArchived: Boolean,
        postAuthorId: Long,
        postType: PostType,
        postContent: String,
        postMedia: List<String>,
    ) {
        dao.updatePost(from.copy(
            updatedAt = Instant.now(),
            authorId = postAuthorId,
            type = postType,
            content = postContent.trim(),
            media = postMedia,
            archived = isArchived
        ))
    }
    suspend fun likePost(post: PostEntity) {
        dao.updatePost(post.copy(liked = !post.liked))
    }
    suspend fun bookmarkPost(post: PostEntity) {
        dao.updatePost(post.copy(bookmarked = !post.bookmarked))
    }
    //endregion

    //region COMMENTS
    suspend fun insertComment(
        atPost: Long,
        ofAuthor: Long,
        content: String
    ) {
        dao.insertComment(CommentEntity(
            postId = atPost,
            authorId = ofAuthor,
            createdAt = Instant.now(),
            updatedAt = null,
            content = content,
            liked = false,
            metadata = emptyMap()
        ))
    }
    fun getAllCommentsOf(postId: Long) = dao.getAllCommentsOf(postId)
    suspend fun deleteComment(comment: CommentEntity) = dao.deleteComment(comment)
    suspend fun updateComment(
        from: CommentEntity,
        ofAuthor: Long,
        content: String
    ) {
        dao.updateComment(from.copy(
            authorId = ofAuthor,
            updatedAt = Instant.now(),
            content = content
        ))
    }
    suspend fun likeComment(comment: CommentEntity) {
        dao.updateComment(comment.copy(liked = !comment.liked))
    }
    //endregion
}