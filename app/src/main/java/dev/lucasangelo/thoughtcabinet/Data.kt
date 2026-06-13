package dev.lucasangelo.thoughtcabinet

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.Instant

// NOTE: would prob be easier to do with Realm

@Entity
data class Persona(
    @PrimaryKey(autoGenerate = true)
    val id: Long,

    val name: String,
    val profilePic: String?,
    val shortDesc: String,
    val createdAt: Instant,
    val updatedAt: Instant,

    val data: PersonaData
)
@Serializable
data class PersonaData(
    val values: List<String>,
    val goals: List<String>,
    val quotes: List<String>,
    val colorPalette: List<String>,
    val imageInpos: List<String>,
    val musicInpos: List<String>,
)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Persona::class,
            parentColumns = ["id"],
            childColumns = ["authorId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Post::class,
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
data class Post(
    @PrimaryKey(autoGenerate = true)
    val id : Long,

    val authorId : Long,
    val commentOf : Long?,

    val createdAt: Instant,
    val updatedAt: Instant,
    val mood: String,
    val type: PostType,

    val content: String,
    val media: List<String>,

    val liked: Boolean,
    val bookmarked: Boolean,
)
enum class PostType {
    NOTE,
    ARTICLE,
    MEDIA,
    LINK,
    COMMENT,
}

// TODO: make all type converters nullables (?)
class Converters {
    @TypeConverter
    fun personaDataToJson(data: PersonaData): String =
        Json.encodeToString(data)
    @TypeConverter
    fun jsonToPersonaData(jsonString: String): PersonaData =
        Json.decodeFromString(jsonString)

    @TypeConverter
    fun fromInstant(value: Instant): Long =
        value.toEpochMilli()
    @TypeConverter
    fun toInstant(value: Long): Instant =
        Instant.ofEpochMilli(value)

    @TypeConverter
    fun stringListToJson(data: List<String>): String =
        Json.encodeToString(data)
    @TypeConverter
    fun jsonToStringList(jsonString: String): List<String> =
        Json.decodeFromString(jsonString)
}