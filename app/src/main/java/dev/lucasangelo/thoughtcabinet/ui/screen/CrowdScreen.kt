package dev.lucasangelo.thoughtcabinet.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.lucasangelo.thoughtcabinet.R
import dev.lucasangelo.thoughtcabinet.ui.component.CleanScaffold
import kotlinx.serialization.Serializable

@Serializable
object CrowdRoute

@Composable
fun CrowdScreen() {
    val listState = rememberLazyListState()

    CleanScaffold(
        title = "Your Personas",
        icon = R.drawable.icon_profile,
        topBarActionName = "Settings",
        topBarActionIcon = R.drawable.icon_settings,
        onTopBarActionClicked = { },
        listState = listState
    ) { innerPadding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.fillMaxSize(),
            state = listState
        ) {
            items((1..100).toList()) { item ->
                if (item == 1) Spacer(Modifier.height(innerPadding))
                Text("Item ${item}")
            }
        }
    }
}