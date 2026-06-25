package dev.lucasangelo.thoughtcabinet.util

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Picture
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import dev.lucasangelo.thoughtcabinet.R
import java.io.File
import java.io.FileOutputStream

fun createBitmapFromPicture(picture: Picture): Bitmap {
    val bitmap = createBitmap(picture.width, picture.height)
    val canvas = android.graphics.Canvas(bitmap)
    canvas.drawPicture(picture)
    return bitmap
}

val sharedPostsDir = "shared-posts/" // NOTE: declared at app/src/main/res/xml/provider_paths.xml
val sharedPostFile = "shared-post.png"

fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri {
    val file = File(context.cacheDir, sharedPostsDir + sharedPostFile)
    file.parentFile?.mkdirs()

    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}

fun shareImage(context: Context, uri: Uri) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"

        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_post_text))
        clipData = ClipData.newRawUri(null, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    val chooserIntent = Intent.createChooser(shareIntent, context.getString(R.string.share_image_via)).apply {
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(chooserIntent)
}