package dev.lucasangelo.thoughtcabinet.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.lucasangelo.thoughtcabinet.R
import dev.lucasangelo.thoughtcabinet.ui.component.CleanScaffold
import kotlinx.serialization.Serializable

@Serializable
object ThoughtsRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThoughtsScreen(){
    val listState = rememberLazyListState()

    CleanScaffold(
        title = "Thought Cabinet",
        icon = R.drawable.logo,
        topBarActionName = "Search",
        topBarActionIcon = R.drawable.icon_search,
        onTopBarActionClicked = { },
        listState = listState
    ) { topBarSpacing ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.fillMaxSize(),
            state = listState
        ) {
            items((1..100).toList()) { item ->
                if (item == 1) Spacer(Modifier.height(topBarSpacing))
                Text("Item ${item}")
            }
        }
    }
}