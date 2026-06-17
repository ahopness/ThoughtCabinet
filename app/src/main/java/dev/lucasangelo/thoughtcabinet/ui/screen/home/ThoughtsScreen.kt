package dev.lucasangelo.thoughtcabinet.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.lucasangelo.thoughtcabinet.R
import dev.lucasangelo.thoughtcabinet.ui.component.CleanScaffold
import kotlinx.serialization.Serializable

@Serializable
object ThoughtsRoute

@Composable
fun ThoughtsScreen(rootNavController: NavController){
    val listState = rememberLazyListState()

    CleanScaffold(
        title = "Thought Cabinet",
        icon = R.drawable.logo,
        topBarActionName = "Search",
        topBarActionIcon = R.drawable.icon_search,
        onTopBarActionClicked = { },
        listState = listState
    ) { topBarSpacing, navBarSpacing ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.fillMaxSize(),
            state = listState
        ) {
            item {
                Spacer(Modifier.height(topBarSpacing))
            }

            item {
                Text(
                    text = "TODO",
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(vertical = 128.dp)
                        .fillMaxWidth(),
                )
            }

            item {
                Spacer(Modifier.height(navBarSpacing))
            }
        }
    }
}