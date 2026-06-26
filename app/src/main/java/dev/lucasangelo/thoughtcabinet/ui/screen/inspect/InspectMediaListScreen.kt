package dev.lucasangelo.thoughtcabinet.ui.screen.inspect

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import dev.lucasangelo.thoughtcabinet.ui.component.PagerScaffold
import dev.lucasangelo.thoughtcabinet.ui.component.PagerScaffoldContent
import dev.lucasangelo.thoughtcabinet.util.mediaDir
import dev.lucasangelo.thoughtcabinet.util.swipeToDismiss
import io.github.kdroidfilter.composemediaplayer.VideoPlayerSurface
import io.github.kdroidfilter.composemediaplayer.rememberVideoPlayerState
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class InspectMediaListRoute(val list: List<String>, val startAt: Int = 0, val mediaFolder: String = mediaDir)

@Composable
fun InspectMediaListScreen(
    list: List<String>,
    startAt: Int = 0,
    mediaFolder: String = mediaDir,
    rootNavController: NavController
) {
    val context = LocalContext.current

    PagerScaffold(
        title = "",
        backgroundColor = Color.Black,
        onGoBackRequest = { rootNavController.popBackStack() },
        pageCount = list.size,
        initialPage = startAt
    ) { pagerState, page, offsetDistance, onNextPageRequested ->
        list.map { media -> {
            PagerScaffoldContent(
                pageOffsetDistance = offsetDistance,
                spacing = 0.dp
            ) {
                val mediaFile = remember(media) {
                    File(context.filesDir, mediaFolder + media)
                }

                val modifier = Modifier
                    .fillMaxWidth()
                    .swipeToDismiss({
                        rootNavController.popBackStack()
                    })

                if (mediaFile.extension == "mp4") {
                    val playerState = rememberVideoPlayerState()
                    LaunchedEffect(mediaFile) {
                        playerState.loop = true
                        playerState.openUri(mediaFile.path)
                    }
                    VideoPlayerSurface(
                        playerState = playerState,
                        modifier = modifier
                    )
                } else {
                    AsyncImage(
                        model = mediaFile,
                        contentDescription = null,
                        modifier = modifier
                    )
                }
            }
        } }
    }
}