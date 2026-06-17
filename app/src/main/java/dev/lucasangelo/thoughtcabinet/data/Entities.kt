package dev.lucasangelo.thoughtcabinet.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.time.Instant

// TODO: bookmarks (?)

@Entity
data class PersonaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,

    val createdAt: Instant,
    val updatedAt: Instant?,

    val name: String,
    val description: String,
    val profilePic: String?,
    val colorTheme: Int,

    val inspirations: List<PersonaInspiration>,

    val metadata: Map<String, String>,
)
@Serializable
data class PersonaInspiration(
    val type: PersonaInspirationType,
    val content: String,

    val metadata: Map<String, String>,
)
@Serializable
enum class PersonaInspirationType {
    TEXT,
    MEDIA,
    LINK
}

@Entity(
    foreignKeys = [
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
        )
    ],
    indices = [
        Index("authorId"),
        Index("commentOf"),
    ]
)
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,

    val createdAt: Instant,
    val updatedAt: Instant?,

    val authorId : Long,
    val commentOf : Long?,

    val type: PostType,

    val content: String,
    val media: List<String>,

    val mood: String,
    val liked: Boolean,

    val metadata: Map<String, String>,
)
enum class PostType {
    NOTE,
    ARTICLE,
    MEDIA,
    LINK,
    COMMENT,
}