package dev.lucasangelo.thoughtcabinet.util

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
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
