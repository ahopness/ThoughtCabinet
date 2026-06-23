package dev.lucasangelo.thoughtcabinet

import android.app.Application
import dev.lucasangelo.thoughtcabinet.data.AppDatabase
import dev.lucasangelo.thoughtcabinet.data.AppRepository

class MainApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val repository: AppRepository by lazy { AppRepository(database.dao, this) }
}