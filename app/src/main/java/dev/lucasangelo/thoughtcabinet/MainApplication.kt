package dev.lucasangelo.thoughtcabinet

import android.app.Application
import dev.lucasangelo.thoughtcabinet.data.AppDatabase

class MainApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
}