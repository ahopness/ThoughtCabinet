package dev.lucasangelo.thoughtcabinet.ui.screen.inspect

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import dev.lucasangelo.thoughtcabinet.MainApplication
import dev.lucasangelo.thoughtcabinet.R
import dev.lucasangelo.thoughtcabinet.data.CommentEntity
import dev.lucasangelo.thoughtcabinet.data.PersonaEntity
import dev.lucasangelo.thoughtcabinet.data.PostEntity
import dev.lucasangelo.thoughtcabinet.ui.component.AuthorSelect
import dev.lucasangelo.thoughtcabinet.ui.component.CleanIconButton
import dev.lucasangelo.thoughtcabinet.ui.component.CleanScaffold
import dev.lucasangelo.thoughtcabinet.ui.component.DeleteConfirmationDialog
import dev.lucasangelo.thoughtcabinet.ui.component.FloatingNavigationActionItem
import dev.lucasangelo.thoughtcabinet.ui.component.FloatingNavigationBar
import dev.lucasangelo.thoughtcabinet.ui.component.FloatingNavigationExpandableItem
import dev.lucasangelo.thoughtcabinet.ui.component.FloatingTopBar
import dev.lucasangelo.thoughtcabinet.ui.component.PersonaProfilePicture
import dev.lucasangelo.thoughtcabinet.ui.component.Post
import dev.lucasangelo.thoughtcabinet.ui.component.floatingNavigationBarPadding
import dev.lucasangelo.thoughtcabinet.ui.component.floatingTopBarPadding
import dev.lucasangelo.thoughtcabinet.ui.component.pagerScaffoldContentSpacing
import dev.lucasangelo.thoughtcabinet.util.darken
import dev.lucasangelo.thoughtcabinet.util.formatInstant
import dev.lucasangelo.thoughtcabinet.util.lighten
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class InspectPostRoute(val postId: Long, val requestComment: Boolean = false)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectPostScreen(
    postId: Long,
    thoughts: Map<Long, PostEntity>,
    crowd: Map<Long, PersonaEntity>,
    requestComment: Boolean = false,
    rootNavController: NavController,
    rootShowSnackbar: (String) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current

    val application = context.applicationContext as MainApplication
    val repository = application.repository
    val viewModel: InspectPostViewModel = viewModel(
        factory = viewModelFactory {
            initializer { InspectPostViewModel(repository, application, postId) }
        }
    )

    val thought = remember(postId, thoughts) { thoughts[postId] } ?: return
    val thoughtAuthor = crowd[thought.authorId] ?: return

    val comments by viewModel.comments.collectAsStateWithLifecycle()

    var isRequestingComment by remember { mutableStateOf(requestComment) }
    var editingComment by remember { mutableStateOf<CommentEntity?>(null) }

    CleanScaffold(
        backgroundColor = Color(thoughtAuthor.colorTheme).darken(),
        topBar = {
            FloatingTopBar(
                title = stringResource(R.string.post),
                canGoBack = true,
                onGoBackRequest = { rootNavController.popBackStack() },
            )
        }
    ) {
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item { Spacer(Modifier.height(floatingTopBarPadding/1.5f)) }

            item {
                Post(
                    isStandalone = true,
                    postId,
                    thoughts,
                    crowd,
                    onPostDeleted = {
                        rootNavController.popBackStack()
                    },
                    onCommentRequested = {},
                    rootNavController,
                    modifier = Modifier
                )
            }

            if (comments.isNotEmpty())
                items(comments, key = { it.id }) { comment ->
                    val commentAuthor = crowd[comment.authorId] ?: return@items
                    Comment(
                        comment,
                        commentAuthor,
                        onCommentLiked = { coroutineScope.launch {
                            repository.likeComment(comment)
                        } },
                        onEditRequest = {
                            editingComment = it
                            isRequestingComment = true
                        },
                        onDeletionRequest = {
                            viewModel.deleteComment(it)
                        },
                        rootNavController
                    )
                }
            else
                item {
                    Text(
                        text = stringResource(R.string.really_quiet_in_here),
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(horizontal = 48.dp)
                            .padding(top = 128.dp)
                            .fillMaxWidth(),
                    )
                }

            item { Spacer(Modifier.height(floatingNavigationBarPadding/1.5f)) }
        }

        FloatingNavigationBar(
            tabItems = emptyList(),
            actionItems = listOf(
                FloatingNavigationExpandableItem(
                    icon = R.drawable.icon_add,
                    title = stringResource(R.string.add),
                    showTitle = false,
                    items = listOf(
                        FloatingNavigationActionItem(
                            icon = R.drawable.icon_comment,
                            title = stringResource(R.string.comment),
                            showTitle = true,
                            action = {
                                editingComment = null
                                isRequestingComment = true
                            }
                        )
                    )
                )
            )
        )

        val sheetState = rememberModalBottomSheetState()
        val onDismissRequest: () -> Unit = {
            coroutineScope.launch {
                sheetState.hide()
            }.invokeOnCompletion {
                isRequestingComment = false
            }
        }

        if (isRequestingComment) {
            EditCommentModal(
                editingComment,
                postId,
                sheetState,
                onDismissRequest,
                crowd,
                rootShowSnackbar,
                viewModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Comment(
    comment: CommentEntity,
    author: PersonaEntity,
    onCommentLiked: (CommentEntity) -> Unit,
    onEditRequest: (CommentEntity) -> Unit,
    onDeletionRequest: (CommentEntity) -> Unit,
    rootNavController: NavController,
) {
    var showOptionsModal by remember { mutableStateOf(false) }
    var showDeletionRequest by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.padding(horizontal = 24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            PersonaProfilePicture(
                author.profilePic,
                modifier = Modifier
                    .size(48.dp)
                    .clickable(onClick = {
                        rootNavController.navigate(
                            InspectPersonaRoute(author.id)
                        )
                    } )
            )

            SelectionContainer(Modifier.weight(1f)) {
                Text(comment.content)
            }

            Icon(
                painter = painterResource(
                    id =
                        if (comment.liked)
                            R.drawable.icon_hearted
                        else
                            R.drawable.icon_heart
                ),
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clickable(onClick = { onCommentLiked(comment) })
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(R.drawable.icon_more),
                contentDescription = stringResource(R.string.options),
                modifier = Modifier
                    .size(48.dp)
                    .clickable(onClick = { showOptionsModal = true })
            )
            Text(
                text = formatInstant(comment.createdAt, "MMMM d")
                    .replaceFirstChar { it.titlecase() },
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(0.5f)
            )
        }
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
            CleanIconButton(
                action = stringResource(R.string.edit),
                icon = R.drawable.icon_edit,
                onClick = {
                    onEditRequest(comment)
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
        }
    }
    if (showDeletionRequest) {
        DeleteConfirmationDialog(
            text = stringResource(R.string.delete_post_warning),
            onDismiss = { showDeletionRequest = false },
            onConfirm = { onDeletionRequest(comment) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCommentModal(
    editingComment: CommentEntity?,
    postId: Long,
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    crowd: Map<Long, PersonaEntity>,
    rootShowSnackbar: (String) -> Unit,
    viewModel: InspectPostViewModel,
) {
    val context = LocalContext.current
    var commentCreationBackgroundColor by remember { mutableStateOf(
        Color(crowd[editingComment?.authorId]?.colorTheme ?: Color.Black.toArgb()).darken()
    ) }
    val animatedCommentCreationBackgroundColor
            by animateColorAsState(commentCreationBackgroundColor)

    var commentAuthor by remember { mutableStateOf(editingComment?.authorId ?: null) }
    var commentContent by remember { mutableStateOf(editingComment?.content ?: "") }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
        containerColor = animatedCommentCreationBackgroundColor
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(pagerScaffoldContentSpacing),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = stringResource(
                    if (editingComment == null)
                        R.string.add_a_comment
                    else
                        R.string.edit_your_comment
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            var authorSelectIsError by remember { mutableStateOf(false) }
            AuthorSelect(
                commentAuthor,
                onAuthorChanged = {
                    commentAuthor = it
                    authorSelectIsError = false
                },
                onAuthorColorAcquired = {
                    commentCreationBackgroundColor = it
                },
                crowd.values.toList(),
                borderColor =
                    if (authorSelectIsError)
                        Color.Red.lighten(.85f)
                    else
                        Color.Gray,
                modifier = Modifier.fillMaxWidth()
            )

            var contentIsError by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = commentContent,
                onValueChange = { commentContent = it },
                label = { Text(stringResource(R.string.comment_input_label)) },
                maxLines = 6,
                minLines = 4,
                isError = contentIsError,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Button(onClick = {
                    if (commentAuthor == null) {
//                        rootShowSnackbar(context.getString(R.string.error_comment_needs_author)) // NOTE: renders below bottom sheet
                        authorSelectIsError = true
                        return@Button
                    }
                    if (commentContent.isEmpty()) {
//                        rootShowSnackbar(context.getString(R.string.error_comment_cannot_empty))
                        contentIsError = true
                        return@Button
                    }

                    if (editingComment == null)
                        viewModel.insertComment(
                            atPost = postId,
                            ofAuthor = commentAuthor!!,
                            commentContent
                        )
                    else
                        viewModel.updateComment(
                            editingComment,
                            commentAuthor!!,
                            commentContent
                        )

                    rootShowSnackbar(context.getString(R.string.comment_posted_success))
                    onDismissRequest()
                }) {
                    if (editingComment == null)
                        Text(stringResource(R.string.post_comment))
                    else
                        Text(stringResource(R.string.edit_comment))
                }
            }
        }
    }
}