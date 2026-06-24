package dev.lucasangelo.thoughtcabinet

import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.request.allowHardware
import dev.lucasangelo.thoughtcabinet.data.AppDatabase
import dev.lucasangelo.thoughtcabinet.data.AppRepository

class MainApplication : Application(), SingletonImageLoader.Factory {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val repository: AppRepository by lazy { AppRepository(database.dao, this) }

    override fun newImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .allowHardware(false) // NOTE: disable hardware bitmaps globally to support screenshot capturing
            .build()
    }
}