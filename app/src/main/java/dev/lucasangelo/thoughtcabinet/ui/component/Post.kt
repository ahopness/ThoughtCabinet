package dev.lucasangelo.thoughtcabinet.ui.component

import android.graphics.Picture
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import dev.lucasangelo.thoughtcabinet.MainApplication
import dev.lucasangelo.thoughtcabinet.R
import dev.lucasangelo.thoughtcabinet.data.PersonaEntity
import dev.lucasangelo.thoughtcabinet.data.PostEntity
import dev.lucasangelo.thoughtcabinet.data.PostType
import dev.lucasangelo.thoughtcabinet.ui.screen.edit.EditPostRoute
import dev.lucasangelo.thoughtcabinet.ui.screen.inspect.InspectMediaListRoute
import dev.lucasangelo.thoughtcabinet.ui.screen.inspect.InspectPersonaRoute
import dev.lucasangelo.thoughtcabinet.ui.screen.inspect.InspectPostRoute
import dev.lucasangelo.thoughtcabinet.util.LinkMetadata
import dev.lucasangelo.thoughtcabinet.util.createBitmapFromPicture
import dev.lucasangelo.thoughtcabinet.util.darken
import dev.lucasangelo.thoughtcabinet.util.fetchLinkMetadata
import dev.lucasangelo.thoughtcabinet.util.formatInstant
import dev.lucasangelo.thoughtcabinet.util.mediaDir
import dev.lucasangelo.thoughtcabinet.util.saveBitmapToCache
import dev.lucasangelo.thoughtcabinet.util.shareImage
import kotlinx.coroutines.launch
import java.io.File

val postItemSpacing = 16.dp
val postIconSize = 54.dp

@Composable
fun Post(
    isStandalone: Boolean,
    postId: Long,
    thoughts: Map<Long, PostEntity>,
    crowd: Map<Long, PersonaEntity>,
    onPostDeleted: () -> Unit = {},
    onCommentRequested: (PostEntity) -> Unit,
    rootNavController: NavController,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current
    val application = context.applicationContext as MainApplication
    val repository = application.repository

    // NOTE: direct calls to repository is a bad practice, doing it anyway because it's only for simple tasks
    val onDeletionRequest: (PostEntity) -> Unit = { coroutineScope.launch {
        repository.deletePost(it)
        onPostDeleted()
    } }
    val onBlockPersonaRequest: (PersonaEntity) -> Unit = { coroutineScope.launch {
        repository.blockPersona(it)
    } }
    val onLikeRequested: (PostEntity) -> Unit = { coroutineScope.launch {
        repository.likePost(it)
    } }
    val onBookmarkRequested: (PostEntity) -> Unit = { coroutineScope.launch {
        repository.bookmarkPost(it)
    } }

    val postEntity = thoughts[postId] ?: return
    val authorEntity = crowd[postEntity.authorId] ?: return
    val repostChain = remember(postId, thoughts.values) {
        buildMap {
            var current = postEntity
            while (current.repostOf != null) {
                val repost = thoughts[current.repostOf] ?: break
                val author = crowd[repost.authorId] ?: break
                put(author, repost)
                current = repost
            }
        }
    }

    val picture = remember { Picture() }
    val captureModifier = Modifier.drawWithCache {
        val width = size.width.toInt()
        val height = size.height.toInt()

        onDrawWithContent {
            val pictureCanvas = androidx.compose.ui.graphics.Canvas(
                picture.beginRecording(width, height)
            )
            draw(this, layoutDirection, pictureCanvas, size) {
                this@onDrawWithContent.drawContent()
            }
            picture.endRecording()

            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawPicture(picture)
            }
        }
    }

    Column(modifier.fillMaxWidth()) {
        var backgroundColor by remember { mutableStateOf(Color.Black) }
        Column(captureModifier) {
            repostChain.entries.reversed().forEach { entry ->
                val repostBackgroundColor = Color(entry.key.colorTheme).darken()
                PostHeader(
                    entry.key,
                    entry.value,
                    showOptions = false,
                    onDeletionRequest,
                    onBlockPersonaRequest,
                    rootNavController,
                    onClickRoute =
                        if (!isStandalone)
                            InspectPostRoute(postEntity.id)
                        else
                            InspectPostRoute(entry.value.id),
                    modifier = Modifier.background(repostBackgroundColor)
                )

                PostContent(
                    entry.value,
                    rootNavController,
                    modifier = Modifier.background(repostBackgroundColor)
                )
            }

            backgroundColor = Color(authorEntity.colorTheme).darken()
            PostHeader(
                authorEntity,
                postEntity,
                onDeletionRequest = onDeletionRequest,
                onBlockPersonaRequest = onBlockPersonaRequest,
                rootNavController = rootNavController,
                onClickRoute =
                    if (!isStandalone)
                        InspectPostRoute(postEntity.id)
                    else
                        null,
                modifier = Modifier.background(backgroundColor)
            )

            PostContent(
                postEntity,
                rootNavController,
                modifier = Modifier.background(backgroundColor)
            )
        }

        PostActions(
            isStandalone,
            postEntity,
            onLikeRequested,
            onBookmarkRequested,
            onCommentRequested,
            rootNavController,
            picture,
            modifier = Modifier.background(backgroundColor)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostHeader(
    authorEntity: PersonaEntity,
    postEntity: PostEntity,
    showOptions: Boolean = true,
    onDeletionRequest: (PostEntity) -> Unit,
    onBlockPersonaRequest: (PersonaEntity) -> Unit,
    rootNavController: NavController,
    onClickRoute: Any?,
    modifier: Modifier,
) {
    var showOptionsModal by remember { mutableStateOf(false) }
    var showDeletionRequest by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .padding(postItemSpacing)
            .clickable(onClick = {
                if (onClickRoute != null)
                    rootNavController.navigate(onClickRoute)
            })
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

        if (showOptions)
            Icon(
                painter = painterResource(R.drawable.icon_more),
                contentDescription = stringResource(R.string.options),
                modifier = Modifier
                    .size(48.dp)
                    .clickable(onClick = { showOptionsModal = true })
            )
    }

    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    val onDismissRequest: () -> Unit = {
        coroutineScope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            showOptionsModal = false
        }
    }
    if (showOptionsModal) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = onDismissRequest,
            containerColor = Color.Black
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CleanIconButton(
                    action = stringResource(R.string.edit),
                    icon = R.drawable.icon_edit,
                    onClick = {
                        rootNavController.navigate(
                            EditPostRoute(postEntity.id, postEntity.repostOf)
                        )
                        onDismissRequest()
                    }
                )
                CleanIconButton(
                    action = stringResource(R.string.delete),
                    icon = R.drawable.icon_delete,
                    color = Color.Red,
                    onClick = {
                        showDeletionRequest = true
                        onDismissRequest()
                    },
                )

                Icon(
                    painter = painterResource(R.drawable.divider_horizontal),
                    contentDescription = null,
                    modifier = Modifier
                        .size(54.dp)
                )

                CleanIconButton(
                    action = if (authorEntity.blocked) stringResource(R.string.unblock_persona) else stringResource(R.string.block_persona),
                    icon = R.drawable.icon_block,
    //                color = Color.Red,
                    onClick = {
                        onBlockPersonaRequest(authorEntity)
                        onDismissRequest()
                    },
                )
            }
        }
    }
    if (showDeletionRequest) {
        DeleteConfirmationDialog(
            text = stringResource(R.string.delete_post_warning),
            onDismiss = { showDeletionRequest = false },
            onConfirm = { onDeletionRequest(postEntity) }
        )
    }
}

@Composable
fun PostContent(
    postEntity: PostEntity,
    rootNavController: NavController,
    modifier: Modifier,
) {
    val onMediaClicked: (List<String>, Int) -> Unit = { list, startAt ->
        rootNavController.navigate(InspectMediaListRoute(list, startAt))
    }

    when (postEntity.type) {
        PostType.NOTE -> {
            PostContentNote(
                postEntity,
                onMediaClicked,
                modifier
            )
        }
        PostType.REEL -> {
            PostContentReel(
                postEntity,
                onMediaClicked,
                modifier
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
    modifier: Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        SelectionContainer {
            Text(
                text = postEntity.content,
                modifier = Modifier
                    .padding(horizontal = postItemSpacing * 2)
                    .then(
                        other =
                            if (postEntity.media.isEmpty())
                                Modifier.padding(vertical = postItemSpacing * 2)
                            else
                                Modifier.padding(bottom = postItemSpacing)
                    )
            )
        }

        if (postEntity.media.isNotEmpty())
            Grid(
                config = {
                    if (postEntity.media.size == 1)
                        repeat(1){ column(1f) }
                    else
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
                            .aspectRatio(1f / 1f)
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
    modifier: Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(postItemSpacing*2),
        modifier = modifier.fillMaxWidth()
    ) {
        val context = LocalContext.current
        val pagerState = rememberPagerState(pageCount = { postEntity.media.size })
        if (postEntity.media.size > 1)
            Box {
                HorizontalPager(
                    pagerState,
                    modifier = Modifier.aspectRatio(1f/1f)
                ) { page ->
                    AsyncImage(
                        model = File(context.filesDir, mediaDir + postEntity.media[page]),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = { onMediaClicked(postEntity.media, page) })
                    )
                }

                if (postEntity.media.size > 1)
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .wrapContentHeight()
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp),
                    ) {
                        repeat(pagerState.pageCount) { iteration ->
                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(8.dp)
                                    .background(
                                        color =
                                            if (pagerState.currentPage == iteration)
                                                Color.LightGray
                                            else
                                                Color.LightGray.copy(0.25f)
                                    )
                            )
                        }
                    }
            }
        else
            AsyncImage(
                model = File(context.filesDir, mediaDir + postEntity.media[0]),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { onMediaClicked(postEntity.media, 0) })
            )

        if (postEntity.content.isNotEmpty())
            SelectionContainer {
                Text(
                    text = postEntity.content,
                    modifier = Modifier.padding(horizontal = postItemSpacing*2)
                )
            }
    }
}
@Composable
fun PostContentLink(
    postEntity: PostEntity,
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

        val context = LocalContext.current
        LaunchedEffect(postEntity.content) {
            linkMetadata = fetchLinkMetadata(postEntity.content, context)
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
                text = '[' + (linkMetadata?.title ?: stringResource(R.string.loading)) + ']',
                modifier = Modifier.width(250.dp)
            )
            Icon(
                painter = painterResource(R.drawable.icon_redirect),
                contentDescription = stringResource(R.string.open_link),
                modifier = Modifier
                    .size(postIconSize)
            )
        }

    }
}

@Composable
fun PostActions(
    isStandalone: Boolean,
    postEntity: PostEntity,
    onLikeRequested: (PostEntity) -> Unit,
    onBookmarkRequested: (PostEntity) -> Unit,
    onCommentRequested: (PostEntity) -> Unit,
    rootNavController: NavController,
    picture: Picture,
    modifier: Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .padding(postItemSpacing)
    ) {
        if (!isStandalone)
            Icon(
                painter = painterResource(R.drawable.icon_comment),
                contentDescription = stringResource(R.string.comment),
                modifier = Modifier
                    .size(postIconSize)
                    .clickable(onClick = { onCommentRequested(postEntity) })
            )
        Icon(
            painter =
                if (postEntity.liked)
                    painterResource(R.drawable.icon_hearted)
                else
                    painterResource(R.drawable.icon_heart),
            contentDescription = stringResource(R.string.like),
            modifier = Modifier
                .size(postIconSize)
                .clickable(onClick = { onLikeRequested(postEntity) })
        )
        Icon(
            painter = painterResource(R.drawable.icon_repost),
            contentDescription = stringResource(R.string.repost),
            modifier = Modifier
                .size(postIconSize)
                .clickable(onClick = {
                    rootNavController.navigate(
                        EditPostRoute(null, repostOf = postEntity.id)
                    )
                })
        )
        Icon(
            painter =
                if (postEntity.bookmarked)
                    painterResource(R.drawable.icon_bookmarked)
                else
                    painterResource(R.drawable.icon_bookmark),
            contentDescription = stringResource(R.string.bookmark),
            modifier = Modifier
                .size(postIconSize)
                .clickable(onClick = { onBookmarkRequested(postEntity) })
        )
        Icon(
            painter = painterResource(R.drawable.icon_share),
            contentDescription = stringResource(R.string.share),
            modifier = Modifier
                .size(postIconSize)
                .clickable(onClick = { coroutineScope.launch {
                    shareImage(
                        context,
                        uri = saveBitmapToCache(
                            context,
                            bitmap = createBitmapFromPicture(picture)
                        )
                    )
                } } )
        )
    }
}