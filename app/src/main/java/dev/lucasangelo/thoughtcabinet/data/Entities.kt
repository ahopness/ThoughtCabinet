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
    val id: Long,

    val createdAt: Instant,
    val updatedAt: Instant?,

    val name: String,
    val bio: String,
    val profilePic: String?,
    val colorTheme: Int,

    val trait: List<PersonaTrait>,

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
        ForeignKey(
            entity = PostEntity::class,
            parentColumns = ["id"],
            childColumns = ["commentOf"],
            onDelete = ForeignKey.CASCADE
        ),
    ],
    indices = [
        Index("repostOf"),
        Index("authorId"),
        Index("commentOf"),
    ]
)
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,

    val repostOf: Long?,

    val createdAt: Instant,
    val updatedAt: Instant?,

    val authorId : Long,
    val commentOf : Long?,

    val type: PostType,

    val mood: String,
    val content: String,
    val media: List<String>,

    val liked: Boolean,
    val archived: Boolean,

    val metadata: Map<String, String>,
)
enum class PostType {
    NOTE,
    ARTICLE,
    MEDIA,
    LINK,
    COMMENT,
}