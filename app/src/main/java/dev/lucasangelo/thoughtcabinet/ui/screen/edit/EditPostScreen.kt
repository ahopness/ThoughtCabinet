package dev.lucasangelo.thoughtcabinet.ui.screen.edit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import dev.lucasangelo.thoughtcabinet.MainApplication
import dev.lucasangelo.thoughtcabinet.R
import dev.lucasangelo.thoughtcabinet.data.PersonaEntity
import dev.lucasangelo.thoughtcabinet.data.PostType
import dev.lucasangelo.thoughtcabinet.ui.component.CleanIconButton
import dev.lucasangelo.thoughtcabinet.ui.component.DeleteConfirmationDialog
import dev.lucasangelo.thoughtcabinet.ui.component.PagerScaffold
import dev.lucasangelo.thoughtcabinet.ui.component.PagerScaffoldContent
import dev.lucasangelo.thoughtcabinet.ui.component.PersonaProfilePicture
import dev.lucasangelo.thoughtcabinet.ui.component.CleanDescriptionButton
import dev.lucasangelo.thoughtcabinet.util.darken
import dev.lucasangelo.thoughtcabinet.util.draftsDir
import dev.lucasangelo.thoughtcabinet.util.mediaDir
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
            if (repostOf != null) // NOTE: fugly :( but works :)
                if (id != null)
                    "Edit Your Repost"
                else
                    "Create A Repost"
            else
                if (id != null)
                    "Edit Your Post"
                else
                    "Create A Post",
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
        Text("First, choose the kind of post")

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
                title = "Note",
                description = "Quick, text-centric with additional media",
                onClick = { onTryChangePostType(PostType.NOTE) }
            )
            CleanDescriptionButton(
                icon = R.drawable.icon_media_post_alt,
                title = "Reel",
                description = "Refined, image-centric with additional text",
                onClick = { onTryChangePostType(PostType.REEL) }
            )
            CleanDescriptionButton(
                icon = R.drawable.icon_link_post,
                title = "Link",
                description = "Delegated, outside site, music or video",
                onClick = { onTryChangePostType(PostType.LINK) }
            )
        }

        pendingPostTypeChange?.let {
            DeleteConfirmationDialog(
                text = "The content of your post is not empty, if you delete the content now you won't be able to recover it later.",
                onDismiss = { pendingPostTypeChange = null },
                onConfirm = { onPostTypeChanged(pendingPostTypeChange!!) },
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
    PagerScaffoldContent(pageOffsetDistance) {
        Text("Then, add the content you want")

        EditPostAuthorSelect(
            crowd,
            viewModel
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
                    rootShowSnackbar
                )
            }
            PostType.REEL -> {
                EditPostReelContent(
                    viewModel,
                    onPostContentChanced,
                    rootShowSnackbar
                )
            }
            PostType.LINK -> {
                OutlinedTextField(
                    value = viewModel.postContent,
                    onValueChange = onPostContentChanced,
                    label = { Text("Paste your link here") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPostAuthorSelect(
    crowd: List<PersonaEntity>,
    viewModel: EditPostViewModel,
) {
    val selectedPersona = crowd.find { it.id == viewModel.postAuthorId }

    var showBottomSheet by remember { mutableStateOf(false) }

    OutlinedButton(
        shape = RoundedCornerShape(6.dp),
        contentPadding = PaddingValues(4.dp),
        border = BorderStroke(width = 1.dp, color = Color.Gray),
        onClick = { showBottomSheet = true },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PersonaProfilePicture(
                    profilePic = selectedPersona?.profilePic,
                    modifier = Modifier
                        .size(64.dp)
                        .padding(6.dp)
                )
                Text(
                    text = selectedPersona?.name ?: "Select Author"
                )
            }
            Image(
                painter = painterResource(R.drawable.icon_expand),
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(32.dp)
            )
        }
    }

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    if (showBottomSheet) {
        EditPostAuthorSelectModal(
            sheetState,
            crowd,
            onDismissRequest = {
                scope.launch {
                    sheetState.hide()
                }.invokeOnCompletion {
                    showBottomSheet = false
                }
            },
            onPersonaSelected = { index ->
                if (viewModel.hasLoadedPost)
                    viewModel.postAuthorId = crowd[index].id
            }
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPostAuthorSelectModal(
    sheetState: SheetState,
    crowd: List<PersonaEntity>,
    onDismissRequest: () -> Unit,
    onPersonaSelected: (Int) -> Unit,
) {
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
        containerColor = Color.Black
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            crowd.forEachIndexed { index, entity ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp)
                        .clickable(onClick = {
                            onPersonaSelected(index)
                            onDismissRequest()
                        })
                ) {
                    PersonaProfilePicture(
                        profilePic = entity.profilePic,
                        modifier = Modifier.size(54.dp)
                    )
                    Text(
                        text = entity.name,
                    )
                }
            }
        }
    }
}

@Composable
fun EditPostNoteContent(
    viewModel: EditPostViewModel,
    onPostContentChanced: (String) -> Unit,
    rootShowSnackbar: (String) -> Unit,
) {
    OutlinedTextField(
        value = viewModel.postContent,
        onValueChange = onPostContentChanced,
        label = { Text("What's up?") },
        maxLines = 8,
        minLines = 6,
        modifier = Modifier.fillMaxWidth(),
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
) {
    EditPostMediaList(
        large = true,
        viewModel,
        onNotifyError = rootShowSnackbar,
    )

    OutlinedTextField(
        value = viewModel.postContent,
        onValueChange = onPostContentChanced,
        label = { Text("Write Caption") },
        maxLines = 4,
        minLines = 1,
        modifier = Modifier.fillMaxWidth(),
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPostMediaList(
    large: Boolean,
    viewModel: EditPostViewModel,
    onNotifyError: (String) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris: List<Uri> ->
            coroutineScope.launch {
                uris.forEach { uri ->
                    val newMedia = viewModel.importMedia(uri)
                    if(newMedia == null) {
                        onNotifyError("ERROR: Couldn't open selected image.")
                        return@launch
                    }

                    if (viewModel.hasLoadedPost)
                        viewModel.postMedia += newMedia
                }
            }
        }
    )

    val context = LocalContext.current
    val cachedMedias = remember { mutableStateMapOf<String, Boolean>() }
    LaunchedEffect(viewModel.postMedia) {
        viewModel.postMedia.forEach {
            val mediaFile = File(context.cacheDir, draftsDir + it)
            cachedMedias[it] = mediaFile.exists()
        }
    }

    var pendingMediaForManipulation by remember { mutableStateOf<String?>(null) }

    LazyRow(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        items(viewModel.postMedia) { media ->
            Box(
                modifier = Modifier.border(
                    border = BorderStroke(width = 1.dp, color = Color.Gray),
                    shape = RoundedCornerShape(6.dp)
                )
            ) {
                AsyncImage(
                    model =
                        if (cachedMedias[media] == true)
                            File(context.cacheDir, draftsDir + media)
                        else
                            File(context.filesDir, mediaDir + media),
                    contentDescription = null,
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
                )
                Image(
                    painter = painterResource(R.drawable.icon_more),
                    contentDescription = "Options",
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
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    ))
                },
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Image(
                        painter = painterResource(R.drawable.icon_camera),
                        contentDescription = null,
                        modifier = Modifier.size(if (large) 64.dp else 32.dp)
                    )
                    Text(
                        text = "Add Media",
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
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
                    action = "Delete",
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
                    Image(
                        painter = painterResource(R.drawable.divider_horizontal),
                        contentDescription = null,
                        modifier = Modifier.size(54.dp),
                    )

                if (canMoveLeft)
                    CleanIconButton(
                        action = "Move Left",
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
                        action = "Move Right",
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
    val coroutineScope = rememberCoroutineScope()
    val onClick = {
        coroutineScope.launch {
            if (viewModel.postType == PostType.REEL) {
                if (viewModel.postMedia.isEmpty()){
                    rootShowSnackbar("ERROR: You need at least one media in your reel!")
                    return@launch
                }
            } else {
                if (viewModel.postContent.trim().isEmpty()) {
                    rootShowSnackbar("ERROR: Your post cannot be empty!")
                    return@launch
                }
            }

            if (viewModel.postAuthorId == null) {
                rootShowSnackbar("ERROR: Your post needs an author!")
                return@launch
            }

            viewModel.commitMedia()

            if (viewModel.post != null) {
                viewModel.updatePost(viewModel.post!!, archiving)
                rootShowSnackbar("Post updated successfully!")
            } else {
                viewModel.insertPost(viewModel.postIsRepostOf, archiving)
                rootShowSnackbar("Post created successfully!")
            }

            rootNavController.popBackStack()
        }
    }

    if (archiving)
        OutlinedButton(
            border = BorderStroke(width = 1.dp, color = Color.Gray),
            onClick = { onClick() }
        ) {
            Text("Archive Post")
        }
    else {
        Button(onClick = { onClick() }) {
            Text(
                if (viewModel.post != null)
                    "Edit Post"
                else
                    "Create Post"
            )
        }
    }
}