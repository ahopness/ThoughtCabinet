package dev.lucasangelo.thoughtcabinet.ui.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalGridApi
import androidx.compose.foundation.layout.Grid
import androidx.compose.foundation.layout.GridFlow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.lucasangelo.thoughtcabinet.R
import dev.lucasangelo.thoughtcabinet.data.AppDatabase
import dev.lucasangelo.thoughtcabinet.ui.component.CleanScaffold
import dev.lucasangelo.thoughtcabinet.ui.component.PersonaProfilePicture
import dev.lucasangelo.thoughtcabinet.ui.component.SimpleFloatingExtendedTopBar
import dev.lucasangelo.thoughtcabinet.ui.component.floatingNavigationBarPadding
import dev.lucasangelo.thoughtcabinet.ui.component.floatingExtendedTopBarPadding
import dev.lucasangelo.thoughtcabinet.ui.screen.edit.EditPersonaRoute
import dev.lucasangelo.thoughtcabinet.ui.screen.inspect.InspectPersonaRoute
import dev.lucasangelo.thoughtcabinet.util.darken
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
object CrowdRoute

@OptIn(ExperimentalGridApi::class)
@Composable
fun CrowdScreen(rootNavController: NavController) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    CleanScaffold(
        topBar = {
            SimpleFloatingExtendedTopBar(
                title = "Your Personas",
                icon = R.drawable.icon_profile,
                onIconClicked = { coroutineScope.launch { listState.animateScrollToItem(0) } },
                actionName = "Settings",
                actionIcon = R.drawable.icon_settings,
                onActionClick = { /* TODO: settings route */ },
                listState = listState,
            )
        }
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.fillMaxSize(),
            state = listState
        ) {
            item {
                Spacer(Modifier.height(floatingExtendedTopBarPadding))
            }

            item {
                val context = LocalContext.current
                val database = remember { AppDatabase.getInstance(context).dao }
                val crowd by database.getAllPersonas().collectAsState(initial = emptyList())

                Grid(
                    config = {
                        repeat(2){ column(0.5f) }
                        gap(0.dp)
                        flow = GridFlow.Row
                    },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    crowd.forEach { persona ->
                        CrowdCard(
                            title = persona.name,
                            description = persona.description,
                            backgroundColor = Color(persona.colorTheme),
                            onClick = { rootNavController.navigate(InspectPersonaRoute(persona.id)) /* TODO: persona route */ }
                        ) {
                            PersonaProfilePicture(
                                profilePic = persona.profilePic,
                                modifier = Modifier.size(72.dp)
                            )
                        }
                    }

                    CrowdCard(
                        title = "New",
                        description = "Create new persona.",
                        backgroundColor = Color.DarkGray,
                        onClick = { rootNavController.navigate(EditPersonaRoute(null)) }
                    ) {
                        Image(
                            painter = painterResource(R.drawable.icon_add),
                            contentDescription = null,
                            modifier = Modifier.size(72.dp)
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(floatingNavigationBarPadding))
            }
        }
    }
}

@Composable
fun CrowdCard(
    title: String,
    description: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f/1f)
            .background(color = backgroundColor.darken())
            .clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(12.dp)
        ) {
            content()

            Spacer(Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                text = description,
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}