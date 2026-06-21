package dev.lucasangelo.thoughtcabinet.ui.screen.edit

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import dev.lucasangelo.thoughtcabinet.MainApplication
import dev.lucasangelo.thoughtcabinet.R
import dev.lucasangelo.thoughtcabinet.data.PersonaEntity
import dev.lucasangelo.thoughtcabinet.data.PostType
import dev.lucasangelo.thoughtcabinet.ui.component.DeleteConfirmationDialog
import dev.lucasangelo.thoughtcabinet.ui.component.PagerScaffold
import dev.lucasangelo.thoughtcabinet.ui.component.PagerScaffoldContent
import dev.lucasangelo.thoughtcabinet.ui.component.PersonaProfilePicture
import dev.lucasangelo.thoughtcabinet.ui.component.TypeDescriptionButton
import dev.lucasangelo.thoughtcabinet.util.darken
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class EditPostRoute(val id: Long?, val childOf: Long? = null)

@Composable
fun EditPostScreen(
    id: Long?,
    childOf: Long?,
    crowd: List<PersonaEntity>,
    rootNavController: NavController,
    rootShowSnackbar: (String) -> Unit,
) {
    val context = LocalContext.current

    val application = context.applicationContext as MainApplication
    val database = application.database
    val viewModel: EditPostViewModel = viewModel(
        factory = viewModelFactory {
            initializer { EditPostViewModel(database.dao, application) }
        }
    )

    LaunchedEffect(id, childOf) {
        viewModel.fetchPost(id, childOf)
    }

    val animatedPersonaColorTheme by animateColorAsState(viewModel.personaColorTheme.darken())
    PagerScaffold(
        title =
            if (id != null)
                "Edit Your Post"
            else
                "Add A Post",
        backgroundColor = animatedPersonaColorTheme,
        canGoBack = true,
        onGoBackRequest = { rootNavController.popBackStack() },
        pageCount = 3,
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
                PostTypeContent(
                    pageOffsetDistance = offsetDistance,
                    viewModel,
                    onNextPageRequested
                )
            },
            {
                PostTypeAuthor(
                    pageOffsetDistance = offsetDistance,
                    crowd,
                    viewModel,
                    rootShowSnackbar,
                    rootNavController
                )
            },
        )
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
            if (viewModel.postContent.isNotEmpty() &&
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
            PostTypeDescriptionButton(
                type = PostType.NOTE,
                icon = R.drawable.icon_note_post,
                title = "Note",
                description = "Quick, text-centric with additional media",
                onTypeChanged = onTryChangePostType
            )
            PostTypeDescriptionButton(
                type = PostType.ARTICLE,
                icon = R.drawable.icon_article_post,
                title = "Article",
                description = "Refined, formatted text and media",
                onTypeChanged = onTryChangePostType
            )
            PostTypeDescriptionButton(
                type = PostType.MEDIA,
                icon = R.drawable.icon_media_post_alt,
                title = "Media",
                description = "Artsy, image-centric with additional text",
                onTypeChanged = onTryChangePostType
            )
            PostTypeDescriptionButton(
                type = PostType.LINK,
                icon = R.drawable.icon_link_post,
                title = "Link",
                description = "Delegated, outside sites, musics or videos",
                onTypeChanged = onTryChangePostType
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
fun PostTypeDescriptionButton(
    type: PostType,
    icon: Int,
    title: String,
    description: String,
    onTypeChanged: (PostType) -> Unit
) {
    TypeDescriptionButton(
        icon = icon,
        title = title,
        description = description,
        onClick = { onTypeChanged(type) }
    )
}

@Composable
fun PostTypeContent(
    pageOffsetDistance: Float,
    viewModel: EditPostViewModel,
    onNextPageRequested: () -> Unit,
) {

}

@Composable
fun PostTypeAuthor(
    pageOffsetDistance: Float,
    crowd: List<PersonaEntity>,
    viewModel: EditPostViewModel,
    rootShowSnackbar: (String) -> Unit,
    rootNavController: NavController,
) {
    PagerScaffoldContent(pageOffsetDistance) {
        Text("Finally, who wrote this")

        var personaId by remember { mutableIntStateOf(0) }

        Box {
            var expanded by remember { mutableStateOf(false) }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { expanded = !expanded })
            ) {
                PersonaProfilePicture(
                    profilePic =
                        if (crowd.isEmpty() || viewModel.postAuthorId == null)
                            null
                        else
                            crowd[personaId].profilePic,
                    modifier = Modifier
                        .size(64.dp)
                        .padding(4.dp)
                )
                Text(
                    text =
                        if (crowd.isEmpty() || viewModel.postAuthorId == null)
                            "Choose persona..."
                        else
                            crowd[personaId].name
                )
                Image(
                    painter = painterResource(R.drawable.icon_expand),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
            }

            Box(Modifier.align(Alignment.BottomEnd)) {
                DropdownMenu(
                    expanded,
                    onDismissRequest = { expanded = false },
                    containerColor = Color.Black,
                ) {
                    crowd.forEachIndexed { index, entity ->
                        DropdownMenuItem(
                            text = {
                                Text(entity.name)
                            },
                            trailingIcon = {
                                PersonaProfilePicture(
                                    profilePic = entity.profilePic,
                                    modifier = Modifier.size(32.dp)
                                )
                            },
                            onClick = {
                                personaId = index
                                if (viewModel.hasLoadedPost)
                                    viewModel.postAuthorId = crowd[personaId].id

                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        Text("And in what mood")

        OutlinedTextField(
            value = viewModel.postMood,
            onValueChange = {
                if (viewModel.hasLoadedPost)
                    viewModel.postMood = it
            },
            label = { Text("Feeling") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        EditPostFinishButton(
            viewModel,
            rootShowSnackbar,
            rootNavController
        )
    }
}
@Composable
fun EditPostFinishButton(
    viewModel: EditPostViewModel,
    rootShowSnackbar: (String) -> Unit,
    rootNavController: NavController,
) {
    val coroutineScope = rememberCoroutineScope()
    Button(onClick = {
        coroutineScope.launch {
            if (viewModel.postType == PostType.MEDIA) {
                if (viewModel.postMedia.isEmpty()){
                    rootShowSnackbar("You need at least one media!")
                    return@launch
                }
            } else {
                if (viewModel.postContent.trim().isEmpty()) {
                    rootShowSnackbar("Your post cannot be empty!")
                    return@launch
                }
            }

            if (viewModel.post != null) {
                viewModel.updatePost(viewModel.post!!)
                rootShowSnackbar("Post updated successfully!")
            } else {
                viewModel.insertPost(viewModel.postParent)
                rootShowSnackbar("Post created successfully!")
            }

            /* TODO: post media import
            if (!viewModel.commitMedia()) {
                rootShowSnackbar("ERROR: Couldn't import media.")
            }
            */

            rootNavController.popBackStack()
        }
    }) {
        Text(
            if (viewModel.post != null)
                "Edit Post"
            else
                "Create Post"
        )
    }
}