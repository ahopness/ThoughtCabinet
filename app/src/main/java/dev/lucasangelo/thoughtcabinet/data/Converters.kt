package dev.lucasangelo.thoughtcabinet.data

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json
import java.time.Instant

// TODO: make all type converters nullables (?)
class Converters {
    @TypeConverter
    fun instantToLong(value: Instant): Long =
        value.toEpochMilli()
    @TypeConverter
    fun longToInstant(value: Long): Instant =
        Instant.ofEpochMilli(value)

    @TypeConverter
    fun stringListToJson(data: List<String>): String =
        Json.encodeToString(data)
    @TypeConverter
    fun jsonToStringList(jsonString: String): List<String> =
        Json.decodeFromString(jsonString)

    @TypeConverter
    fun stringMapToJson(data: Map<String, String>): String =
        Json.encodeToString(data)
    @TypeConverter
    fun jsonToStringMap(jsonString: String): Map<String, String> =
        Json.decodeFromString(jsonString)


    @TypeConverter
    fun inspirationListToJson(data: List<PersonaInspiration>): String =
        Json.encodeToString(data)
    @TypeConverter
    fun jsonToInspirationList(jsonString: String): List<PersonaInspiration> =
        Json.decodeFromString(jsonString)
}