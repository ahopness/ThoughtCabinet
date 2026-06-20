package dev.lucasangelo.thoughtcabinet.util

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream

val draftsDir = "drafts/"
val profilePicDir = "profile-pictures/"
val traitsDir = "persona-traits/"
val mediaDir = "post-media/"

fun getFileExtension(
    context: Context,
    uri: Uri
) : String? {
    val mimeType = context.contentResolver.getType(uri) ?: return null
    return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
}

fun copyUriToInternalStorage(
    context: Context,
    uri: Uri,
    fileParentDir: File,
    fileName: String,
) : File? {
    val inputStream = context.contentResolver.openInputStream(uri)
        ?: return null

    val outputFile = File(fileParentDir, fileName)
    outputFile.parentFile?.mkdirs()

    inputStream.use { input ->
        FileOutputStream(outputFile).use { output ->
            input.copyTo(output)
        }
    }

    return outputFile
}

fun copyInInternalStorage(
    inputFileParentDir: File,
    inputFileName: String,
    outputFileParentDir: File,
    outputFileName: String,
) : File? {
    val inputFile = File(inputFileParentDir, inputFileName)
    if (!inputFile.exists()) return null

    val outputFile = File(outputFileParentDir, outputFileName)
    outputFile.parentFile?.mkdirs()

    return inputFile.copyTo(outputFile, overwrite = true)
}

fun deleteInternalStorageFile(
    fileParentDir: File,
    fileName: String,
) : Boolean {
    val file = File(fileParentDir, fileName)
    return file.delete()
}

fun cleanupDrafts(
    context: Context,
) {
    val drafts = File(context.cacheDir, draftsDir)
    drafts.listFiles()?.forEach { it.delete() }
}