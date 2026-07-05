package dev.lucasangelo.thoughtcabinet.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import coil3.imageLoader
import coil3.util.CoilUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

val draftsDir = "drafts/"
val profilePicDir = "profile-pictures/"
val traitsDir = "persona-traits/"
val mediaDir = "post-media/"
val linkMetadataDir = "links-metadata/"

suspend fun getFileExtension(
    context: Context,
    uri: Uri
) : String? = withContext(Dispatchers.IO) {
    val mimeType = context.contentResolver.getType(uri) ?: return@withContext null
    return@withContext MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
}

suspend fun copyUriToInternalStorage(
    context: Context,
    uri: Uri,
    fileParentDir: File,
    fileName: String,
) : File? = withContext(Dispatchers.IO) {
    val inputStream = context.contentResolver.openInputStream(uri)
        ?: return@withContext null

    val outputFile = File(fileParentDir, fileName)
    outputFile.parentFile?.mkdirs()

    inputStream.use { input ->
        FileOutputStream(outputFile).use { output ->
            input.copyTo(output)
            output.flush()
            output.fd.sync()
        }
    }

    return@withContext outputFile
}

suspend fun copyInInternalStorage(
    inputFileParentDir: File,
    inputFileName: String,
    outputFileParentDir: File,
    outputFileName: String,
) : File? = withContext(Dispatchers.IO) {
    val inputFile = File(inputFileParentDir, inputFileName)
    if (!inputFile.exists()) return@withContext null

    val outputFile = File(outputFileParentDir, outputFileName)
    outputFile.parentFile?.mkdirs()

    return@withContext inputFile.copyTo(outputFile, overwrite = true)
}

suspend fun deleteInternalStorageFile(
    fileParentDir: File,
    fileName: String,
) : Boolean = withContext(Dispatchers.IO) {
    val file = File(fileParentDir, fileName)
    return@withContext file.delete()
}

suspend fun cleanupFolder(
    context: Context,
    folder: String
) = withContext(Dispatchers.IO) {
    val drafts = File(context.cacheDir, folder)
    drafts.listFiles()?.forEach { it.delete() }
}
suspend fun cleanupDrafts(
    context: Context,
) = cleanupFolder(context, draftsDir)
suspend fun cleanupLinkMetadata(
    context: Context,
) = cleanupFolder(context, linkMetadataDir)
fun cleanupImageCacheInMemory (
    context: Context,
) = context.imageLoader.memoryCache?.clear()
fun cleanupImageCacheInDisk (
    context: Context,
) = context.imageLoader.diskCache?.clear()

suspend fun saveMediaToLocalStorage(context: Context, sourceFile: File, fileName: String): Uri? =
withContext(Dispatchers.IO) {
    val resolver = context.contentResolver
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        put(MediaStore.MediaColumns.MIME_TYPE, context.contentResolver.getType(sourceFile.toUri()))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
        }
    }

    val collectionUri =
        if (fileName.endsWith("mp4"))
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        else
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    val destinationUri = resolver.insert(collectionUri, contentValues) ?: return@withContext null

    try {
        resolver.openOutputStream(destinationUri)?.use { outputStream ->
            sourceFile.inputStream().use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        destinationUri
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}