package dev.lucasangelo.thoughtcabinet.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import java.util.Collections.emptyMap
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern

data class LinkMetadata(
    val title: String?,
    val imageUrl: String?,
    val description: String?
)

val cachedLinkMetadata = ConcurrentHashMap<String, LinkMetadata>()
suspend fun fetchLinkMetadata(urlString: String): LinkMetadata = withContext(Dispatchers.IO) {
    try {
        cachedLinkMetadata[urlString]?.let { return@withContext it }

        val url = URL(urlString)
        val connection = url.openConnection()

        // NOTE: added User-Agent to avoid 403s on some sites
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")

        val html = connection.getInputStream().bufferedReader().use { it.readText() }

        val title = extractMetaTag(html, "og:title") ?: extractTitleTag(html)
        val imageUrl = extractMetaTag(html, "og:image")
        val description = extractMetaTag(html, "og:description") ?: extractMetaTagByName(html, "description")

        val metadata = LinkMetadata(title, imageUrl, description)

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