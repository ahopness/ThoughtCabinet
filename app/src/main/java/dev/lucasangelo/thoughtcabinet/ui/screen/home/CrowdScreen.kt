package dev.lucasangelo.thoughtcabinet.ui.screen.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.lucasangelo.thoughtcabinet.R
import dev.lucasangelo.thoughtcabinet.data.PersonaEntity
import dev.lucasangelo.thoughtcabinet.ui.component.CleanScaffold
import dev.lucasangelo.thoughtcabinet.ui.component.FloatingExtendedTopBar
import dev.lucasangelo.thoughtcabinet.ui.component.FloatingExtendedTopBarActionItem
import dev.lucasangelo.thoughtcabinet.ui.component.PersonaProfilePicture
import dev.lucasangelo.thoughtcabinet.ui.component.floatingExtendedTopBarPadding
import dev.lucasangelo.thoughtcabinet.ui.component.floatingNavigationBarPadding
import dev.lucasangelo.thoughtcabinet.ui.screen.inspect.InspectPersonaRoute
import dev.lucasangelo.thoughtcabinet.ui.screen.misc.OnboardingRoute
import dev.lucasangelo.thoughtcabinet.util.darken
import kotlinx.coroutines.launch

@OptIn(ExperimentalGridApi::class)
@Composable
fun CrowdScreen(
    listState: LazyListState,
    crowd: Map<Long, PersonaEntity>,
    rootNavController: NavController,
) {
    val coroutineScope = rememberCoroutineScope()

    CleanScaffold(
        topBar = {
            FloatingExtendedTopBar(
                title = stringResource(R.string.your_personas),
                canGoBack = false,
                iconContent = { modifier, _ ->
                    Icon(
                        painter = painterResource(R.drawable.icon_persona),
                        contentDescription = null,
                        modifier = modifier
                            .clickable(onClick = {
                            coroutineScope.launch { listState.animateScrollToItem(0) }
                        })
                    )
                },
                customOverlayTransparencyColor = Color.Black,
                listState = listState,
                actions = listOf(
                    FloatingExtendedTopBarActionItem(
                        name = stringResource(R.string.help),
                        icon = R.drawable.icon_help,
                        onClick = { rootNavController.navigate(OnboardingRoute) }
                    ),
                )
            )
        }
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.fillMaxSize(),
            state = listState
        ) {
            item { Spacer(Modifier.height(floatingExtendedTopBarPadding)) }

            if (crowd.isEmpty())
                item {
                        Text(
                            text = stringResource(R.string.abyss_stare),
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(horizontal = 48.dp)
                                .padding(top = 128.dp)
                                .fillMaxWidth(),
                        )
                }
            else
                item {
                    Grid(
                        config = {
                            repeat(2){ column(0.5f) }
                            gap(12.dp)
                            flow = GridFlow.Row
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                    ) {
                        crowd.values.forEach { persona ->
                            PersonaTile(
                                title = persona.name,
                                description = persona.bio,
                                backgroundColor = Color(persona.colorTheme),
                                onClick = { rootNavController.navigate(InspectPersonaRoute(persona.id)) }
                            ) {
                                PersonaProfilePicture(
                                    profilePic = persona.profilePic,
                                    modifier = Modifier.size(72.dp)
                                )
                            }
                        }
                    }
                }

            item { Spacer(Modifier.height(floatingNavigationBarPadding + 16.dp)) }
        }
    }
}
@Composable
fun PersonaTile(
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
            .border(
                border = BorderStroke(width = 1.dp, color = Color.Gray),
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp)
        ) {
            content()

            Spacer(Modifier.height(4.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            if (description.isNotEmpty()) {
                Text(
                    text = description,
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}