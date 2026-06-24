package dev.lucasangelo.thoughtcabinet.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    //region PERSONA
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
    //endregion

    //region POSTS
    @Insert
    suspend fun insertPost(post: PostEntity): Long
    @Query("SELECT * FROM PostEntity WHERE id = :id")
    suspend fun getPost(id: Long): PostEntity?
    @Query("SELECT * FROM PostEntity ORDER BY createdAt DESC")
    fun getAllPosts(): Flow<List<PostEntity>>
    @Query( value =
        "SELECT * FROM PostEntity " +
                "INNER JOIN PersonaEntity On PostEntity.authorId = PersonaEntity.id " +
                "WHERE PersonaEntity.blocked = 0 " +
                "AND PostEntity.archived = 0 " +
                "ORDER BY PostEntity.createdAt DESC"
    )
    fun getAllValidPosts(): Flow<List<PostEntity>>
    @Query( value =
        "SELECT * FROM PostEntity " +
        "INNER JOIN PersonaEntity On PostEntity.authorId = PersonaEntity.id " +
        "WHERE PersonaEntity.blocked = 0 " +
        "AND PostEntity.archived = 1 " +
        "ORDER BY PostEntity.createdAt DESC"
    )
    fun getAllArchivedPosts(): Flow<List<PostEntity>>
    @Query( value =
        "SELECT * FROM PostEntity " +
        "INNER JOIN PersonaEntity On PostEntity.authorId = PersonaEntity.id " +
        "WHERE PersonaEntity.blocked = 0 " +
        "AND PostEntity.archived = 0 " +
        "AND PostEntity.bookmarked = 1 " +
        "ORDER BY PostEntity.createdAt DESC"
    )
    fun getAllBookmarkedPosts(): Flow<List<PostEntity>>
    @Query( value =
        "SELECT * FROM PostEntity " +
        "INNER JOIN PersonaEntity On PostEntity.authorId = PersonaEntity.id " +
        "WHERE PersonaEntity.blocked = 1 " +
        "AND PostEntity.archived = 0 " +
        "ORDER BY PostEntity.createdAt DESC"
    )
    fun getAllPostsFromBlockedPersonas(): Flow<List<PostEntity>>
    @Query( value =
        "SELECT * FROM PostEntity " +
        "WHERE authorId = :authorId " +
        "AND archived = 0 " +
        "ORDER BY createdAt DESC"
    )
    fun getAllPostsBy(authorId: Long): Flow<List<PostEntity>>
    @Query( value =
        "SELECT * FROM PostEntity " +
        "WHERE content LIKE '%' || :query || '%' " +
        "AND type != 'LINK' " +
        "ORDER BY createdAt DESC"
    )
    fun searchPosts(query: String): Flow<List<PostEntity>>
    @Update
    suspend fun updatePost(persona: PostEntity)
    @Delete
    suspend fun deletePost(persona: PostEntity)
    //endregion

    //region COMMENTS
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
    //endregion
}