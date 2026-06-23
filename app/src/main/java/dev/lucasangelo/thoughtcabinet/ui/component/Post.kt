package dev.lucasangelo.thoughtcabinet.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalGridApi
import androidx.compose.foundation.layout.Grid
import androidx.compose.foundation.layout.GridFlow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import dev.lucasangelo.thoughtcabinet.R
import dev.lucasangelo.thoughtcabinet.data.PersonaEntity
import dev.lucasangelo.thoughtcabinet.data.PostEntity
import dev.lucasangelo.thoughtcabinet.data.PostType
import dev.lucasangelo.thoughtcabinet.ui.screen.edit.EditPersonaTraitRoute
import dev.lucasangelo.thoughtcabinet.ui.screen.edit.EditPostRoute
import dev.lucasangelo.thoughtcabinet.ui.screen.inspect.InspectMediaListRoute
import dev.lucasangelo.thoughtcabinet.ui.screen.inspect.InspectPersonaRoute
import dev.lucasangelo.thoughtcabinet.util.LinkMetadata
import dev.lucasangelo.thoughtcabinet.util.darken
import dev.lucasangelo.thoughtcabinet.util.draftsDir
import dev.lucasangelo.thoughtcabinet.util.fetchLinkMetadata
import dev.lucasangelo.thoughtcabinet.util.formatInstant
import dev.lucasangelo.thoughtcabinet.util.mediaDir
import kotlinx.coroutines.launch
import java.io.File

val postItemSpacing = 16.dp
val postIconSize = 54.dp

@Composable
fun Post(
    postEntity: PostEntity,
    authorEntity: PersonaEntity,
    onDeletionRequest: (PostEntity) -> Unit,
    onLikeRequested: (PostEntity) -> Unit,
    rootNavController: NavController,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(authorEntity.colorTheme).darken())
    ) {
        PostHeader(
            authorEntity,
            postEntity,
            onDeletionRequest,
            rootNavController
        )

        PostContent(
            postEntity,
            rootNavController
        )

        PostActions(
            postEntity,
            onLikeRequested
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostHeader(
    authorEntity: PersonaEntity,
    postEntity: PostEntity,
    onDeletionRequest: (PostEntity) -> Unit,
    rootNavController: NavController,
) {
    var showManipulationModal by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(postItemSpacing)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(postItemSpacing)
        ) {
            PersonaProfilePicture(
                authorEntity.profilePic,
                modifier = Modifier
                    .size(postIconSize)
                    .clickable(onClick = {
                        rootNavController.navigate(InspectPersonaRoute(authorEntity.id))
                    })
            )

            Column() {
                Text(
                    text = authorEntity.name,
                )
                Text(
                    text = formatInstant(postEntity.createdAt, "MMMM d"),
                    color = Color.White.copy(0.5f)
                )
            }
        }
        Image(
            painter = painterResource(R.drawable.icon_more),
            contentDescription = "More",
            modifier = Modifier
                .size(48.dp)
                .clickable(onClick = { showManipulationModal = true })
        )
    }

    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    val onDismissRequest: () -> Unit = {
        coroutineScope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            showManipulationModal = false
        }
    }
    if (showManipulationModal) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = onDismissRequest,
            containerColor = Color.Black
        ) {
            CleanIconButton(
                action = "Edit",
                icon = R.drawable.icon_edit,
                onClick = {
                    rootNavController.navigate(
                        EditPostRoute(postEntity.id, postEntity.repostOf)
                    )
                    onDismissRequest()
                }
            )
            CleanIconButton(
                action = "Delete",
                icon = R.drawable.icon_delete,
                color = Color.Red,
                onClick = {
                    onDeletionRequest(postEntity)
                    onDismissRequest()
                },
            )
        }
    }
}

@Composable
fun PostContent(
    postEntity: PostEntity,
    rootNavController: NavController,
) {
    val onMediaClicked: (List<String>, Int) -> Unit = { list, startAt ->
        rootNavController.navigate(InspectMediaListRoute(list, startAt))
    }

    when (postEntity.type) {
        PostType.NOTE -> {
            PostContentNote(
                postEntity,
                onMediaClicked
            )
        }
        PostType.REEL -> {
            PostContentReel(
                postEntity,
                onMediaClicked
            )
        }
        PostType.LINK -> {
            PostContentLink(
                postEntity
            )
        }
    }
}
@OptIn(ExperimentalGridApi::class)
@Composable
fun PostContentNote(
    postEntity: PostEntity,
    onMediaClicked: (List<String>, Int) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = postEntity.content,
            modifier = Modifier.padding(postItemSpacing*2)
        )

        if (postEntity.media.isNotEmpty())
            Grid(
                config = {
                    repeat(2){ column(0.5f) }
                    gap(0.dp)
                    flow = GridFlow.Row
                },
                modifier = Modifier.fillMaxSize()
            ) {
                val context = LocalContext.current
                postEntity.media.forEachIndexed { index, media ->
                    AsyncImage(
                        model = File(context.filesDir, mediaDir + media),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .aspectRatio(1f/1f)
                            .clickable(onClick = { onMediaClicked(postEntity.media, index) })
                    )
                }
            }
    }
}
@Composable
fun PostContentReel(
    postEntity: PostEntity,
    onMediaClicked: (List<String>, Int) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(postItemSpacing*2),
        modifier = Modifier.fillMaxWidth()
    ) {
        val context = LocalContext.current
        LazyRow(
            modifier = Modifier.fillMaxWidth().aspectRatio(1f/1f)
        ) {
            itemsIndexed(postEntity.media) { index, media ->
                AsyncImage(
                    model = File(context.filesDir, mediaDir + media),
                    contentDescription = null,
                    contentScale = ContentScale.FillHeight,
                    modifier = Modifier
                        .fillMaxHeight()
                        .clickable(onClick = { onMediaClicked(postEntity.media, index) })
                )
            }
        }

        if (postEntity.content.isNotEmpty())
            Text(
                text = postEntity.content,
                modifier = Modifier.padding(horizontal = postItemSpacing*2)
            )
    }
}
@Composable
fun PostContentLink(
    postEntity: PostEntity
) {
    val uriHandler = LocalUriHandler.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable(onClick = {
            try {
                uriHandler.openUri(postEntity.content)
            } catch (e: Exception) {
//                rootShowSnackbar("ERROR: Could not open URL: $postEntity.content")
            }
        })
    ) {
        var linkMetadata by remember(postEntity.content) { mutableStateOf<LinkMetadata?>(null) }

        LaunchedEffect(postEntity.content) {
            linkMetadata = fetchLinkMetadata(postEntity.content)
        }

        AsyncImage(
            model = linkMetadata?.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .alpha(0.5f)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text(
                text = '[' + (linkMetadata?.title ?: "LOADING...") + ']',
                modifier = Modifier.width(250.dp)
            )
            Image(
                painter = painterResource(R.drawable.icon_redirect),
                contentDescription = "Open Link",
                modifier = Modifier.size(postIconSize)
            )
        }

    }
}

@Composable
fun PostActions(
    postEntity: PostEntity,
    onLikeRequested: (PostEntity) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(postItemSpacing)
    ) {
        Image(
            painter = painterResource(R.drawable.icon_comment),
            contentDescription = "Comment",
            modifier = Modifier.size(postIconSize)
        )
        Image(
            painter =
                if (postEntity.liked)
                    painterResource(R.drawable.icon_hearted)
                else
                    painterResource(R.drawable.icon_heart),
            contentDescription = "Comment",
            modifier = Modifier
                .size(postIconSize)
                .clickable(onClick = { onLikeRequested(postEntity) })
        )
        Image(
            painter = painterResource(R.drawable.icon_repost),
            contentDescription = "Comment",
            modifier = Modifier.size(postIconSize)
        )
        Image(
            painter = painterResource(R.drawable.icon_share),
            contentDescription = "Comment",
            modifier = Modifier.size(postIconSize)
        )
    }
}