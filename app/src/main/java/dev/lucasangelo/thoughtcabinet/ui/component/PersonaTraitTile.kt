package dev.lucasangelo.thoughtcabinet.ui.component

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.lucasangelo.thoughtcabinet.R
import dev.lucasangelo.thoughtcabinet.data.PersonaTraitType
import dev.lucasangelo.thoughtcabinet.util.LinkMetadata
import dev.lucasangelo.thoughtcabinet.util.darken
import dev.lucasangelo.thoughtcabinet.util.draftsDir
import dev.lucasangelo.thoughtcabinet.util.fetchLinkMetadata
import dev.lucasangelo.thoughtcabinet.util.traitsDir
import java.io.File

@Composable
fun PersonaTraitTile(
    type: PersonaTraitType,
    content: String,
    mediaInCache: Boolean = false,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    extras: @Composable BoxScope.() -> Unit = {},
) {
    Box(
        modifier = modifier
            .aspectRatio(1f/1f)
            .background(backgroundColor.darken(0.8f))

    ) {
        val context = LocalContext.current
        val uriHandler = LocalUriHandler.current
        when(type) {
            PersonaTraitType.TEXT ->
                Text(
                    text = "\"$content\"",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    fontStyle = FontStyle.Italic,
                    fontFamily = FontFamily.Serif,
                    modifier = Modifier.align(Alignment.Center)
                )
            PersonaTraitType.MEDIA ->
                AsyncImage(
                    model = File(
                        if (mediaInCache) context.cacheDir else context.filesDir,
                        (if (mediaInCache) draftsDir else traitsDir) + content
                    ),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .aspectRatio(1f / 1f)
                )
            PersonaTraitType.LINK ->
                Box(Modifier.clickable(onClick = {
                        try {
                            uriHandler.openUri(content)
                        } catch (e: Exception) {
                            Log.e("OpenUrl", "Could not open URL: $content", e)
                        }
                })) {
                    var linkMetadata by remember(content) { mutableStateOf<LinkMetadata?>(null) }

                    LaunchedEffect(content) {
                        linkMetadata = fetchLinkMetadata(content)
                    }

                    linkMetadata.let {
                        AsyncImage(
                            model = it?.imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .aspectRatio(1f / 1f)
                                .alpha(0.5f)
                        )

                        Text(
                            text = '[' + (linkMetadata?.title ?: linkMetadata?.description ?: "LOADING...") + ']',
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.Center)
                        )

                        Image(
                            painter = painterResource(R.drawable.icon_redirect),
                            contentDescription = "Open Link",
                            modifier = Modifier
                                .size(54.dp)
                                .align(Alignment.BottomStart)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color.Black.copy(0.5f),
                                            Color.Transparent
                                        ),
                                        center = Offset(0f, Float.POSITIVE_INFINITY),
                                        radius = 125f
                                    )
                                )
                        )
                    }

                }
        }

        extras()
    }
}