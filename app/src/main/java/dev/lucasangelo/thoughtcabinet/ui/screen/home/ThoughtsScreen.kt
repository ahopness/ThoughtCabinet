package dev.lucasangelo.thoughtcabinet.ui.screen.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import dev.lucasangelo.thoughtcabinet.MainApplication
import dev.lucasangelo.thoughtcabinet.R
import dev.lucasangelo.thoughtcabinet.data.PersonaEntity
import dev.lucasangelo.thoughtcabinet.data.PostEntity
import dev.lucasangelo.thoughtcabinet.ui.component.CleanScaffold
import dev.lucasangelo.thoughtcabinet.ui.component.Post
import dev.lucasangelo.thoughtcabinet.ui.component.SimpleFloatingExtendedTopBar
import dev.lucasangelo.thoughtcabinet.ui.component.floatingNavigationBarPadding
import dev.lucasangelo.thoughtcabinet.ui.component.floatingExtendedTopBarPadding
import dev.lucasangelo.thoughtcabinet.ui.screen.inspect.InspectMediaListRoute
import dev.lucasangelo.thoughtcabinet.ui.screen.inspect.InspectPersonaViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
object ThoughtsRoute

@Composable
fun ThoughtsScreen(
    rootNavController: NavController,
    listState: LazyListState,
    thoughts: List<PostEntity>,
    crowd: List<PersonaEntity>,
){
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current

    val application = context.applicationContext as MainApplication
    val viewModel: ThoughtsViewModel = viewModel(
        factory = viewModelFactory {
            initializer { ThoughtsViewModel(application.repository, application) }
        }
    )

    val crowdMap = remember(crowd) { crowd.associateBy { it.id } }

    CleanScaffold(
        topBar = {
            SimpleFloatingExtendedTopBar(
                title = "Thought Cabinet",
                icon = R.drawable.logo,
                onIconClicked = { coroutineScope.launch { listState.animateScrollToItem(0) } },
                actionName = "Search",
                actionIcon = R.drawable.icon_search,
                onActionClick = { /* TODO: search route */ },
                listState = listState,
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
                items(thoughts) { thought ->
                    val postAuthorEntity = crowdMap[thought.authorId] ?: return@items

                    Post(
                        postEntity = thought,
                        authorEntity = postAuthorEntity, // NOTE: might result in NullPointerException (?) but i hope not so cuz im deleting posts on cascade when i kill a persona, gotta watch out for cosmic rays tho
                        onDeletionRequest = { post -> viewModel.deletePost(post) },
                        onLikeRequested = { post -> viewModel.likePost(post) },
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

            item { Spacer(Modifier.height(floatingNavigationBarPadding + 16.dp)) }
        }
    }
}