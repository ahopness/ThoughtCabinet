package dev.lucasangelo.thoughtcabinet.ui.screen.misc

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import dev.lucasangelo.thoughtcabinet.ui.component.FloatingTopBar
import dev.lucasangelo.thoughtcabinet.ui.component.Post
import dev.lucasangelo.thoughtcabinet.ui.component.floatingNavigationBarPadding
import dev.lucasangelo.thoughtcabinet.ui.component.floatingTopBarPadding
import dev.lucasangelo.thoughtcabinet.ui.screen.inspect.InspectPostRoute
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
object SearchRoute

@Composable
fun SearchScreen(
    thoughts: Map<Long, PostEntity>,
    crowd: Map<Long, PersonaEntity>,
    rootNavController: NavController,
) {
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current

    val application = context.applicationContext as MainApplication
    val repository = application.repository
    val viewModel: SearchViewModel = viewModel(
        factory = viewModelFactory {
            initializer { SearchViewModel(repository, application) }
        }
    )

    var search by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()

    CleanScaffold(
        topBar = {
            FloatingTopBar(
                title = "Search",
                canGoBack = true,
                onGoBackRequest = { rootNavController.popBackStack() }
            )
        }
    ) {
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            item { Spacer(Modifier.height(floatingTopBarPadding/1.5f)) }

            item {
                OutlinedTextField(
                    value = search,
                    onValueChange = {
                        search = it
                        viewModel.onSearchQueryChange(it)
                    },
                    singleLine = true,
                    leadingIcon = {
                        Image(
                            painter = painterResource(R.drawable.icon_search),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                    },
                    label = {
                        Text("What are we looking for?")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (searchResults.isEmpty())
                item {
                    Text(
                        text = "Wuthering and Uninhabited.",
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(horizontal = 48.dp)
                            .padding(top = 128.dp)
                            .fillMaxWidth(),
                    )
                }
            else
                items(searchResults, key = { it.id }) { post ->
                    val thoughtAuthor = crowd[post.authorId] ?: return@items

                    val repostChain = remember(post.id, thoughts.values) {
                        buildMap {
                            var current = post
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
                        postEntity = post,
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
                            .clip(RoundedCornerShape(6.dp))
                            .border(
                                border = BorderStroke(width = 1.dp, color = Color.Gray),
                                shape = RoundedCornerShape(6.dp)
                            )
                    )
                }

            item { Spacer(Modifier.height(floatingNavigationBarPadding/1.5f)) }
        }
    }
}