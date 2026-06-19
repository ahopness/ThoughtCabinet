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
    @Query("SELECT * FROM PostEntity WHERE commentOf IS NULL")
    fun getAllFeedPosts(): Flow<List<PostEntity>>
    @Query("SELECT * FROM PostEntity WHERE commentOf = :id")
    fun getAllCommentsOf(id: Long): Flow<List<PostEntity>>
    @Query("SELECT * FROM PostEntity WHERE authorId = :authorId")
    fun getAllPostsBy(authorId: Long): Flow<List<PostEntity>>
    @Query("SELECT * FROM PostEntity WHERE id = :id")
    suspend fun getPost(id: Long): PostEntity?
    @Update
    suspend fun updatePost(persona: PostEntity)
    @Delete
    suspend fun deletePost(persona: PostEntity)
}