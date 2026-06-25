package dev.lucasangelo.thoughtcabinet.ui.screen.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import dev.lucasangelo.thoughtcabinet.MainApplication
import dev.lucasangelo.thoughtcabinet.R
import dev.lucasangelo.thoughtcabinet.data.PersonaEntity
import dev.lucasangelo.thoughtcabinet.data.PostEntity
import dev.lucasangelo.thoughtcabinet.ui.component.CleanScaffold
import dev.lucasangelo.thoughtcabinet.ui.component.FloatingExtendedTopBar
import dev.lucasangelo.thoughtcabinet.ui.component.FloatingExtendedTopBarActionItem
import dev.lucasangelo.thoughtcabinet.ui.component.Post
import dev.lucasangelo.thoughtcabinet.ui.component.floatingExtendedTopBarPadding
import dev.lucasangelo.thoughtcabinet.ui.component.floatingNavigationBarPadding
import dev.lucasangelo.thoughtcabinet.ui.screen.inspect.InspectPostRoute
import dev.lucasangelo.thoughtcabinet.ui.screen.misc.SearchRoute
import dev.lucasangelo.thoughtcabinet.util.cleanupLinkMetadata
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThoughtsScreen(
    listState: LazyListState,
    thoughts: Map<Long, PostEntity>,
    crowd: Map<Long, PersonaEntity>,
    rootNavController: NavController,
    rootShowSnackbar: (String) -> Unit,
){
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current

    val application = context.applicationContext as MainApplication
    val repository = application.repository
    val viewModel: ThoughtsViewModel = viewModel(
        factory = viewModelFactory {
            initializer { ThoughtsViewModel(repository, application) }
        }
    )

    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val feedMode by viewModel.feedMode.collectAsStateWithLifecycle()
    val feed by viewModel.feed.collectAsStateWithLifecycle()

    var showSettingsModal by remember { mutableStateOf(false) }

    CleanScaffold(
        topBar = {
            FloatingExtendedTopBar(
                title = "Thought Cabinet",
                canGoBack = false,
                iconContent = { modifier, _ ->
                    Icon(
                        painter = painterResource(R.drawable.logo),
                        contentDescription = null,
                        modifier = modifier
                            .clickable(onClick = {
                                coroutineScope.launch { listState.animateScrollToItem(0) }
                            }),
                    )
                },
                listState = listState,
                actions = listOf(
                    FloatingExtendedTopBarActionItem(
                        name = "Search",
                        icon = R.drawable.icon_search,
                        onClick = { rootNavController.navigate(SearchRoute) }
                    ),
                    FloatingExtendedTopBarActionItem(
                        name = "Settings",
                        icon = R.drawable.icon_settings,
                        onClick = { showSettingsModal = true }
                    ),
                )
            )
        }
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize(),
            state = listState
        ) {
            item { Spacer(Modifier.height(floatingExtendedTopBarPadding + 16.dp)) }

            if (isLoading)
                item {
                    Text(
                        text = "Thinking...",
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(horizontal = 48.dp)
                            .padding(top = 128.dp)
                            .fillMaxWidth(),
                    )
                }
            else
                if (feed.isEmpty())
                    item {
                        Text(
                            text = "A blank canvas, ready to be given purpose.",
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(horizontal = 48.dp)
                                .padding(top = 128.dp)
                                .fillMaxWidth(),
                        )
                    }
                else
                    items(feed.toList(), key = { it.id }) { post ->
                        Post(
                            isStandalone = false,
                            postId = post.id,
                            thoughts,
                            crowd,
                            onCommentRequested = { post -> rootNavController.navigate(
                                    InspectPostRoute(post.id, requestComment = true)
                            ) },
                            rootNavController = rootNavController,
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(
                                    border = BorderStroke(width = 1.dp, color = Color.Gray),
                                    shape = RoundedCornerShape(6.dp)
                                )
                        )
                    }

            item { Spacer(Modifier.height(floatingNavigationBarPadding + 32.dp)) }
        }
    }

    val sheetState = rememberModalBottomSheetState()
    val onDismissRequest: () -> Unit = {
        coroutineScope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            showSettingsModal = false
        }
    }
    if (showSettingsModal)
        SettingModal(
            feedMode,
            sheetState,
            onDismissRequest,
            viewModel,
            rootShowSnackbar
        )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingModal(
    feedMode: ThoughtsViewModel.FeedMode,
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    viewModel: ThoughtsViewModel,
    rootShowSnackbar: (String) -> Unit,
) {
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
        containerColor = Color.Black
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text("Feed")

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.height(IntrinsicSize.Max)
            ) {
                listOf("Standard", "Bookmarks", "Archives", "Blocked Personas")
                    .forEachIndexed { index, string ->
                        SegmentedButton(
                            label = { Text(
                                text = string,
                                style = MaterialTheme.typography.labelSmall
                            ) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = ThoughtsViewModel.FeedMode.entries.size
                            ),
                            onClick = {
                                viewModel.onFeedModeChanged(ThoughtsViewModel.FeedMode.entries[index])
                            },
                            selected = (index == feedMode.ordinal),
                            modifier = Modifier.fillMaxHeight(),
                        )
                }
            }

            Text("Actions")

            val context = LocalContext.current
            val coroutineScope = rememberCoroutineScope()
            Button(onClick = { coroutineScope.launch {
                cleanupLinkMetadata(context)
            }.invokeOnCompletion {
                rootShowSnackbar("Link cache cleaned up successfully!")
                onDismissRequest()
            } } ) {
                Text("Clean Link Cache")

            }
        }
    }
}
