package dev.lucasangelo.thoughtcabinet.ui.screen.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
import kotlinx.coroutines.launch

@Composable
fun ThoughtsScreen(
    listState: LazyListState,
    thoughts: Map<Long, PostEntity>,
    crowd: Map<Long, PersonaEntity>,
    rootNavController: NavController,
){
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current

    val application = context.applicationContext as MainApplication
    val repository = application.repository

    CleanScaffold(
        topBar = {
            FloatingExtendedTopBar(
                title = "Thought Cabinet",
                canGoBack = false,
                iconContent = { modifier, _ ->
                    Image(
                        painter = painterResource(R.drawable.logo),
                        contentDescription = null,
                        contentScale = ContentScale.Inside,
                        modifier = modifier
                            .clickable(onClick = {
                            coroutineScope.launch { listState.animateScrollToItem(0) }
                        })
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
                        onClick = {}
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

            if (thoughts.isEmpty())
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
                items(thoughts.values.toList(), key = { it.id }) { thought ->
                    val thoughtAuthor = crowd[thought.authorId] ?: return@items

                    val repostChain = remember(thought.id, thoughts.values) {
                        buildMap {
                            var current = thought
                            while (current.repostOf != null) {
                                val repost = thoughts[current.repostOf] ?: break
                                val author = crowd[repost.authorId] ?: break
                                put(author, repost)
                                current = repost
                            }
                        }
                    }

                    Post(
                        isStandalone = false,
                        postEntity = thought,
                        authorEntity = thoughtAuthor,
                        repostChain,
                        onDeletionRequest = { post -> coroutineScope.launch {
                            repository.deletePost(post) // NOTE: direct calls to repository is a bad practice but i didnt wanted to create another viewmodel for such a simple task
                        } },
                        onLikeRequested = { post -> coroutineScope.launch {
                            repository.likePost(post)
                        } },
                        onBookmarkRequested = { post -> coroutineScope.launch {
                            repository.bookmarkPost(post)
                        } },
                        onCommentRequested = { post -> rootNavController.navigate(
                                InspectPostRoute(post.id, requestComment = true)
                        ) },
                        rootNavController,
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
}
