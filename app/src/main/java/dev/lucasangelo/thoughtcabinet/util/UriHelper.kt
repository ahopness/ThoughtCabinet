package dev.lucasangelo.thoughtcabinet.util

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream

fun getFileExtension(context: Context, uri: Uri): String? {
    val mimeType = context.contentResolver.getType(uri) ?: return null
    return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
}

fun copyUriToInternalStorage(
    context: Context,
    uri: Uri,
    fileName: String
): File? {
    val inputStream = context.contentResolver.openInputStream(uri)
        ?: return null

    val outputFile = File(context.filesDir, fileName)
    outputFile.parentFile?.mkdirs()

    inputStream.use { input ->
        FileOutputStream(outputFile).use { output ->
            input.copyTo(output)
        }
    }

    return outputFile
}

fun deleteInternalStorageFile(
    context: Context,
    fileName: String
): Boolean {
    val file = File(context.filesDir, fileName)
    return file.delete()
}