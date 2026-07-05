package dev.lucasangelo.thoughtcabinet.util

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern

@Serializable
data class LinkMetadata(
    val title: String?,
    val imageUrl: String?,
    val description: String?
)

val cachedLinkMetadata = ConcurrentHashMap<String, LinkMetadata>()
suspend fun fetchLinkMetadata(urlString: String, context: Context): LinkMetadata = withContext(Dispatchers.IO) {
    try {
        cachedLinkMetadata[urlString]?.let {
            return@withContext it
        }

        val cacheFile = File(context.cacheDir, linkMetadataDir + urlString.hashCode())
        cacheFile.parentFile?.mkdirs()
        if (cacheFile.exists()) {
            val metadata: LinkMetadata = Json.decodeFromString(cacheFile.readText())
            cachedLinkMetadata[urlString] = metadata
            return@withContext metadata
        }

        val url = URL(urlString)
        val connection = url.openConnection()

        // NOTE: some websites dont like default user agents, other do, no idea which one to choose
        // TODO: rollback this is if it ever gives users headaches
//        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
        connection.setRequestProperty("User-Agent", "ThoughtCabinet/1.0 (dev.lucas.angelo@gmail.com)")

        val html = connection.getInputStream().bufferedReader().use { it.readText() }

        val title = extractMetaTag(html, "og:title") ?: extractTitleTag(html)
        val imageUrl = extractMetaTag(html, "og:image")
        val description = extractMetaTag(html, "og:description") ?: extractMetaTagByName(html, "description")

        val metadata = LinkMetadata(title, imageUrl, description)

        cacheFile.writeText(Json.encodeToString(metadata))

        cachedLinkMetadata[urlString] = metadata

        return@withContext metadata
    } catch (e: Exception) {
        return@withContext LinkMetadata(null, null, null)
    }
}

private fun extractMetaTag(html: String, property: String): String? {
    val pattern = Pattern.compile("<meta[^>]*property=[\"']$property[\"'][^>]*content=[\"']([^\"']*)[\"']", Pattern.CASE_INSENSITIVE)
    val matcher = pattern.matcher(html)
    return if (matcher.find()) matcher.group(1) else null
}

private fun extractMetaTagByName(html: String, name: String): String? {
    val pattern = Pattern.compile("<meta[^>]*name=[\"']$name[\"'][^>]*content=[\"']([^\"']*)[\"']", Pattern.CASE_INSENSITIVE)
    val matcher = pattern.matcher(html)
    return if (matcher.find()) matcher.group(1) else null
}

private fun extractTitleTag(html: String): String? {
    val pattern = Pattern.compile("<title>(.*?)</title>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
    val matcher = pattern.matcher(html)
    return if (matcher.find()) matcher.group(1).trim() else null
}