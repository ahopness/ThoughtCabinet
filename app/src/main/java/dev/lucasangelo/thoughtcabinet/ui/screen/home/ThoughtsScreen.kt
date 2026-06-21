package dev.lucasangelo.thoughtcabinet.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.lucasangelo.thoughtcabinet.R
import dev.lucasangelo.thoughtcabinet.data.PostEntity
import dev.lucasangelo.thoughtcabinet.ui.component.CleanScaffold
import dev.lucasangelo.thoughtcabinet.ui.component.SimpleFloatingExtendedTopBar
import dev.lucasangelo.thoughtcabinet.ui.component.floatingNavigationBarPadding
import dev.lucasangelo.thoughtcabinet.ui.component.floatingExtendedTopBarPadding
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
object ThoughtsRoute

@Composable
fun ThoughtsScreen(
    rootNavController: NavController,
    listState: LazyListState,
    thoughts: List<PostEntity>,
){
    val coroutineScope = rememberCoroutineScope()

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
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.fillMaxSize(),
            state = listState
        ) {
            item { Spacer(Modifier.height(floatingExtendedTopBarPadding)) }

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

            item { Spacer(Modifier.height(floatingNavigationBarPadding)) }
        }
    }
}