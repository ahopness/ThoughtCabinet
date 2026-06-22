package dev.lucasangelo.thoughtcabinet.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Insert
    suspend fun insertPersona(persona: PersonaEntity): Long
    @Query("SELECT * FROM PersonaEntity")
    fun getAllPersonas(): Flow<List<PersonaEntity>>
    @Query("SELECT * FROM PersonaEntity WHERE id = :id")
    suspend fun getPersona(id: Long): PersonaEntity?
    @Update
    suspend fun updatePersona(persona: PersonaEntity)
    @Delete
    suspend fun deletePersona(persona: PersonaEntity)

    @Insert
    suspend fun insertPost(post: PostEntity): Long
    @Query("SELECT * FROM PostEntity WHERE id = :id")
    suspend fun getPost(id: Long): PostEntity?
    @Query("SELECT * FROM PostEntity WHERE archived = 0")
    fun getAllPosts(): Flow<List<PostEntity>>
    @Query("SELECT * FROM PostEntity WHERE archived = 1")
    fun getAllArchivedPosts(): Flow<List<PostEntity>>
    @Query("SELECT * FROM PostEntity WHERE bookmarked = 1")
    fun getAllBookmarkedPosts(): Flow<List<PostEntity>>
    @Query("SELECT * FROM PostEntity WHERE authorId = :authorId")
    fun getAllPostsBy(authorId: Long): Flow<List<PostEntity>>
    @Update
    suspend fun updatePost(persona: PostEntity)
    @Delete
    suspend fun deletePost(persona: PostEntity)

    @Insert
    suspend fun insertComment(comment: CommentEntity): Long
    @Query("SELECT * FROM CommentEntity WHERE postId = :postId")
    fun getAllCommentsOf(postId: Long): Flow<List<CommentEntity>>
    @Query("SELECT * FROM CommentEntity WHERE authorId = :authorId")
    fun getAllCommentsBy(authorId: Long): Flow<List<CommentEntity>>
    @Update
    suspend fun updateComment(comment: CommentEntity)
    @Delete
    suspend fun deleteComment(comment: CommentEntity)
}