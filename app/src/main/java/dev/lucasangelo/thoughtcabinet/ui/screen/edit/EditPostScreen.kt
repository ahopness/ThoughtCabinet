package dev.lucasangelo.thoughtcabinet.ui.screen.edit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import dev.lucasangelo.thoughtcabinet.MainApplication
import dev.lucasangelo.thoughtcabinet.R
import dev.lucasangelo.thoughtcabinet.data.PersonaEntity
import dev.lucasangelo.thoughtcabinet.data.PostType
import dev.lucasangelo.thoughtcabinet.ui.component.AuthorSelect
import dev.lucasangelo.thoughtcabinet.ui.component.CleanDescriptionButton
import dev.lucasangelo.thoughtcabinet.ui.component.CleanIconButton
import dev.lucasangelo.thoughtcabinet.ui.component.DeleteConfirmationDialog
import dev.lucasangelo.thoughtcabinet.ui.component.PagerScaffold
import dev.lucasangelo.thoughtcabinet.ui.component.PagerScaffoldContent
import dev.lucasangelo.thoughtcabinet.ui.component.PostContentMediaItem
import dev.lucasangelo.thoughtcabinet.ui.component.pagerScaffoldContentSpacing
import dev.lucasangelo.thoughtcabinet.util.darken
import dev.lucasangelo.thoughtcabinet.util.draftsDir
import dev.lucasangelo.thoughtcabinet.util.mediaDir
import dev.lucasangelo.thoughtcabinet.viewmodel.EditPostViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class EditPostRoute(val id: Long?, val repostOf: Long? = null)

@Composable
fun EditPostScreen(
    id: Long?,
    repostOf: Long?,
    crowd: List<PersonaEntity>,
    rootNavController: NavController,
    rootShowSnackbar: (String) -> Unit,
) {
    val context = LocalContext.current

    val application = context.applicationContext as MainApplication
    val viewModel: EditPostViewModel = viewModel(
        factory = viewModelFactory {
            initializer { EditPostViewModel(application.repository, application) }
        }
    )

    LaunchedEffect(id, repostOf) {
        viewModel.fetchPost(id, repostOf)
    }

    val animatedPersonaColorTheme by animateColorAsState(viewModel.personaColorTheme.darken())
    PagerScaffold(
        title =
            if (repostOf != null) // NOTE: fugly :(, but works :)
                if (id != null)
                    stringResource(R.string.edit_your_repost)
                else
                    stringResource(R.string.create_a_repost)
            else
                if (id != null)
                    stringResource(R.string.edit_your_post)
                else
                    stringResource(R.string.create_a_post),
        backgroundColor = animatedPersonaColorTheme,
        canGoBack = true,
        onGoBackRequest = { rootNavController.popBackStack() },
        pageCount = 2,
        initialPage = if (id != null) 1 else 0
    ) { pagerState, page, offsetDistance, onNextPageRequested ->
        listOf<@Composable () -> Unit>(
            {
                EditPostTypeSelect(
                    pageOffsetDistance = offsetDistance,
                    viewModel,
                    onNextPageRequested
                )
            },
            {
                EditPostContent(
                    pageOffsetDistance = offsetDistance,
                    crowd,
                    viewModel,
                    rootShowSnackbar,
                    rootNavController
                )
            },
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.cleanupMediaDrafts()
        }
    }
}

@Composable
fun EditPostTypeSelect(
    pageOffsetDistance: Float,
    viewModel: EditPostViewModel,
    onNextPageRequested: () -> Unit,
) {
    PagerScaffoldContent(pageOffsetDistance) {
        Text(stringResource(R.string.choose_post_kind))

        var pendingPostTypeChange by remember { mutableStateOf<PostType?>(null) }

        val onPostTypeChanged: (PostType) -> Unit = {
            if (viewModel.hasLoadedPost) {
                if (it != viewModel.postType) {
                    viewModel.postContent = ""
                    viewModel.postMedia = emptyList()
                }

                viewModel.postType = it
                onNextPageRequested()
            }
        }
        val onTryChangePostType: (PostType) -> Unit = {
            if ((viewModel.postContent.isNotEmpty() || viewModel.postMedia.isNotEmpty()) &&
                it != viewModel.postType) {
                pendingPostTypeChange = it
            } else {
                onPostTypeChanged(it)
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            CleanDescriptionButton(
                icon = R.drawable.icon_note_post,
                title = stringResource(R.string.post_kind_note),
                description = stringResource(R.string.post_kind_note_desc),
                onClick = { onTryChangePostType(PostType.NOTE) }
            )
            CleanDescriptionButton(
                icon = R.drawable.icon_media_post_alt,
                title = stringResource(R.string.post_kind_reel),
                description = stringResource(R.string.post_kind_reel_desc),
                onClick = { onTryChangePostType(PostType.REEL) }
            )
            CleanDescriptionButton(
                icon = R.drawable.icon_link_post,
                title = stringResource(R.string.post_kind_link),
                description = stringResource(R.string.post_kind_link_desc),
                onClick = { onTryChangePostType(PostType.LINK) }
            )
        }

        pendingPostTypeChange?.let {
            DeleteConfirmationDialog(
                text = stringResource(R.string.delete_post_content_warning),
                onDismiss = { pendingPostTypeChange = null },
                onConfirm = { onPostTypeChanged(it) },
            )
        }
    }
}

@Composable
fun EditPostContent(
    pageOffsetDistance: Float,
    crowd: List<PersonaEntity>,
    viewModel: EditPostViewModel,
    rootShowSnackbar: (String) -> Unit,
    rootNavController: NavController,
) {
    PagerScaffoldContent(
        pageOffsetDistance,
        spacing = 0.dp
    ) {
        val defaultModifier = Modifier.padding(horizontal = pagerScaffoldContentSpacing)
        Text(stringResource(R.string.add_post_content_prompt))

        AuthorSelect(
            authorId = viewModel.postAuthorId,
            onAuthorChanged = { viewModel.postAuthorId = it },
            onAuthorColorAcquired = { },
            crowd = crowd,
            modifier = defaultModifier
        )

        val onPostContentChanced: (String) -> Unit = {
            if (viewModel.hasLoadedPost)
                viewModel.postContent = it
        }

        when(viewModel.postType) {
            PostType.NOTE -> {
                EditPostNoteContent(
                    viewModel,
                    onPostContentChanced,
                    rootShowSnackbar,
                    defaultModifier
                )
            }
            PostType.REEL -> {
                EditPostReelContent(
                    viewModel,
                    onPostContentChanced,
                    rootShowSnackbar,
                    defaultModifier
                )
            }
            PostType.LINK -> {
                OutlinedTextField(
                    value = viewModel.postContent,
                    onValueChange = onPostContentChanced,
                    label = { Text(stringResource(R.string.paste_link_here)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = defaultModifier.fillMaxWidth(),
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = defaultModifier.fillMaxWidth()
        ) {
            EditPostFinishButton(
                archiving = true,
                viewModel,
                rootShowSnackbar,
                rootNavController
            )
            EditPostFinishButton(
                archiving = false,
                viewModel,
                rootShowSnackbar,
                rootNavController
            )
        }
    }
}

@Composable
fun EditPostNoteContent(
    viewModel: EditPostViewModel,
    onPostContentChanced: (String) -> Unit,
    rootShowSnackbar: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = viewModel.postContent,
        onValueChange = onPostContentChanced,
        label = { Text(stringResource(R.string.post_content_note_label)) },
        maxLines = 8,
        minLines = 6,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        modifier = modifier.fillMaxWidth(),
    )

    EditPostMediaList(
        large = false,
        viewModel,
        onNotifyError = rootShowSnackbar
    )
}
@Composable
fun EditPostReelContent(
    viewModel: EditPostViewModel,
    onPostContentChanced: (String) -> Unit,
    rootShowSnackbar: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    EditPostMediaList(
        large = true,
        viewModel,
        onNotifyError = rootShowSnackbar,
    )

    OutlinedTextField(
        value = viewModel.postContent,
        onValueChange = onPostContentChanced,
        label = { Text(stringResource(R.string.post_content_reel_label)) },
        maxLines = 4,
        minLines = 1,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        modifier = modifier.fillMaxWidth(),
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPostMediaList(
    large: Boolean,
    viewModel: EditPostViewModel,
    onNotifyError: (String) -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val cachedMedias = remember { mutableStateMapOf<String, Boolean>() }
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris: List<Uri> ->
            coroutineScope.launch {
                uris.forEach { uri ->
                    val newMedia = viewModel.importMedia(uri)
                    if(newMedia == null) {
                        onNotifyError(context.getString(R.string.error_could_not_open_media))
                        return@launch
                    }

                    // NOTE: only supporting .mp4 for now due to:
                    // https://github.com/kdroidFilter/ComposeMediaPlayer#-supported-video-formats
                    val prohibitedVideoExtensions = listOf(
                        ".avi", ".mkv", ".mov", ".flv", ".webm", ".wmv", ".3gp", ".hls",
                    )
                    if (prohibitedVideoExtensions.any { newMedia.endsWith(it) }) {
                        onNotifyError(context.getString(R.string.error_video_type_not_supported))
                        return@launch
                    }

                    if (viewModel.hasLoadedPost) {
                        cachedMedias[newMedia] = true
                        viewModel.postMedia += newMedia
                    }
                }
            }
        }
    )

    var pendingMediaForManipulation by remember { mutableStateOf<String?>(null) }

    val mediaSpacing = 24.dp
    LazyRow(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(mediaSpacing),
        modifier = Modifier.fillMaxWidth(),
    ) {
        item { Spacer(Modifier.width(pagerScaffoldContentSpacing - mediaSpacing)) }

        items(viewModel.postMedia) { media ->
            Box(
                modifier = Modifier.border(
                    border = BorderStroke(width = 1.dp, color = Color.Gray),
                    shape = RoundedCornerShape(6.dp)
                )
            ) {
                PostContentMediaItem(
                    mediaFile = remember(media) {
                        if (cachedMedias[media] == true)
                            File(context.cacheDir, draftsDir + media)
                        else
                            File(context.filesDir, mediaDir + media)
                    },
                    contentScale =
                        if (large)
                            ContentScale.FillHeight
                        else
                            ContentScale.Crop,
                    modifier =
                        (if (large)
                            Modifier.height(250.dp)
                        else
                            Modifier.size(100.dp))
                            .clip(RoundedCornerShape(6.dp))
                            .alpha(0.5f)
                )
                Icon(
                    painter = painterResource(R.drawable.icon_more),
                    contentDescription = stringResource(R.string.options),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(48.dp)
                        .clickable(onClick = { pendingMediaForManipulation = media })
                )
            }
        }

        item {
            OutlinedButton(
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(width = 1.dp, color = Color.Gray),
                modifier = Modifier.size(if (large) 250.dp else 100.dp),
                onClick = {
                    picker.launch(PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageAndVideo
                    ))
                },
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.icon_camera),
                        contentDescription = null,
                        modifier = Modifier.size(if (large) 64.dp else 32.dp)
                    )
                    Text(
                        text = stringResource(R.string.add_media),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        item { Spacer(Modifier.width(pagerScaffoldContentSpacing - mediaSpacing)) }
    }

    val sheetState = rememberModalBottomSheetState()
    if (pendingMediaForManipulation != null) {
        val onDismissRequest: () -> Unit = {
            coroutineScope.launch {
                sheetState.hide()
            }.invokeOnCompletion {
                pendingMediaForManipulation = null
            }
        }

        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = onDismissRequest,
            containerColor = Color.Black
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CleanIconButton(
                    action = stringResource(R.string.delete),
                    icon = R.drawable.icon_delete,
                    color = Color.Red,
                    onClick = {
                        if (viewModel.hasLoadedPost)
                            viewModel.postMedia -= pendingMediaForManipulation!!

                        onDismissRequest()
                    },
                )

                val lastTraitIdInList = (viewModel.postMedia.size - 1)
                val mediaId = viewModel.postMedia.indexOf(pendingMediaForManipulation!!)
                val canMoveLeft = mediaId != lastTraitIdInList
                val canMoveRight = mediaId != 0

                if (canMoveLeft || canMoveRight)
                    Icon(
                        painter = painterResource(R.drawable.divider_horizontal),
                        contentDescription = null,
                        modifier = Modifier.size(54.dp),
                    )

                if (canMoveLeft)
                    CleanIconButton(
                        action = stringResource(R.string.move_left),
                        icon = R.drawable.icon_arrow_left,
                        onClick = {
                            if (viewModel.hasLoadedPost) {
                                viewModel.postMedia = viewModel.postMedia.toMutableList().apply {
                                    val media = removeAt(mediaId)
                                    add(mediaId + 1, media)
                                }
                            }
                            onDismissRequest()
                        }
                    )
                if (canMoveRight)
                    CleanIconButton(
                        action = stringResource(R.string.move_right),
                        icon = R.drawable.icon_back,
                        onClick = {
                            if (viewModel.hasLoadedPost) {
                                viewModel.postMedia = viewModel.postMedia.toMutableList().apply {
                                    val media = removeAt(mediaId)
                                    add(mediaId - 1, media)
                                }
                            }
                            onDismissRequest()
                        }
                    )
            }
        }
    }
}

@Composable
fun EditPostFinishButton(
    archiving: Boolean,
    viewModel: EditPostViewModel,
    rootShowSnackbar: (String) -> Unit,
    rootNavController: NavController,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val onClick = {
        coroutineScope.launch {
            if (viewModel.postType == PostType.REEL) {
                if (viewModel.postMedia.isEmpty()){
                    rootShowSnackbar(context.getString(R.string.error_reel_needs_media))
                    return@launch
                }
            } else {
                if (viewModel.postContent.trim().isEmpty()) {
                    if (viewModel.postMedia.isNotEmpty())
                        rootShowSnackbar(context.getString(R.string.error_note_only_pictures_use_reel))
                    else
                        rootShowSnackbar(context.getString(R.string.error_post_cannot_empty))
                    return@launch
                }
            }

            if (viewModel.postAuthorId == null) {
                rootShowSnackbar(context.getString(R.string.error_post_needs_author))
                return@launch
            }

            viewModel.commitMedia()

            if (viewModel.post != null) {
                viewModel.updatePost(viewModel.post!!, archiving)
                rootShowSnackbar(context.getString(R.string.post_updated_success))
            } else {
                viewModel.insertPost(viewModel.postIsRepostOf, archiving)
                rootShowSnackbar(context.getString(R.string.post_created_success))
            }

            rootNavController.popBackStack()
        }
    }

    if (archiving)
        OutlinedButton(
            border = BorderStroke(width = 1.dp, color = Color.Gray),
            onClick = { onClick() }
        ) {
            Text(stringResource(R.string.archive_post))
        }
    else {
        Button(onClick = { onClick() }) {
            Text(
                if (viewModel.post != null)
                    stringResource(R.string.edit_post)
                else
                    stringResource(R.string.create_post)
            )
        }
    }
}