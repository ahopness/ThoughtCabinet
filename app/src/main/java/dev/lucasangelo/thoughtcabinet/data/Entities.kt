package dev.lucasangelo.thoughtcabinet.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.time.Instant

@Entity
data class PersonaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val createdAt: Instant,
    val updatedAt: Instant?,

    val name: String,
    val bio: String,
    val profilePic: String?,
    val colorTheme: Int,

    val traits: List<PersonaTrait>,

    val metadata: Map<String, String>,
)
@Serializable
data class PersonaTrait(
    val type: PersonaTraitType,
    val content: String,

    val metadata: Map<String, String>,
)
@Serializable
enum class PersonaTraitType {
    TEXT,
    MEDIA,
    LINK
}

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = PostEntity::class,
            parentColumns = ["id"],
            childColumns = ["repostOf"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PersonaEntity::class,
            parentColumns = ["id"],
            childColumns = ["authorId"],
            onDelete = ForeignKey.CASCADE
        ),
    ],
    indices = [
        Index("repostOf"),
        Index("authorId"),
    ]
)
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val repostOf: Long?,

    val createdAt: Instant,
    val updatedAt: Instant?,

    val authorId: Long,

    val type: PostType,

    val content: String,
    val media: List<String>,

    val mood: String,

    val liked: Boolean,
    val bookmarked: Boolean,
    val archived: Boolean,

    val metadata: Map<String, String>,
)
enum class PostType {
    NOTE,
    REEL,
    LINK,
}

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = PostEntity::class,
            parentColumns = ["id"],
            childColumns = ["postId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PersonaEntity::class,
            parentColumns = ["id"],
            childColumns = ["authorId"],
            onDelete = ForeignKey.CASCADE
        ),
    ],
    indices = [
        Index("postId"),
        Index("authorId"),
    ]
)
data class CommentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val postId: Long,
    val authorId: Long,

    val content: String,

    val liked: Boolean,

    val metadata: Map<String, String>,
)