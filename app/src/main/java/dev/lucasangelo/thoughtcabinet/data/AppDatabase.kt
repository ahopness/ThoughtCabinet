package dev.lucasangelo.thoughtcabinet.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

// NOTE: inspired by https://developer.android.com/codelabs/android-room-with-a-view-kotlin#7

@Database(
    entities = [
        PersonaEntity::class,
        PostEntity::class,
        CommentEntity::class
    ],
    version = 1,
    autoMigrations = [],
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val dao: AppDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                return Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "thought-cabinet.db"
                ).build().also {
                    instance = it
                }
            }
        }
    }
}
