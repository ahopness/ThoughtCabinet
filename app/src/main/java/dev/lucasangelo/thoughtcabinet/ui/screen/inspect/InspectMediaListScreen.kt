package dev.lucasangelo.thoughtcabinet.ui.screen.inspect

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
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
        list.map { {
            PagerScaffoldContent(
                pageOffsetDistance = offsetDistance,
                spacing = 0.dp
            ) {
                AsyncImage(
                    model = File(context.filesDir, mediaFolder + it),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .swipeToDismiss({
                            rootNavController.getBackStackEntry<InspectMediaListRoute>()
                                .savedStateHandle["dismiss_via_swipe"] = true
                            rootNavController.popBackStack()
                        })
                )
            }
        } }
    }
}