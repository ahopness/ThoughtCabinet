package dev.lucasangelo.thoughtcabinet.ui.screen.inspect

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.navigation.NavController
import androidx.navigation.Navigator
import androidx.navigation.navOptions
import dev.lucasangelo.thoughtcabinet.R
import dev.lucasangelo.thoughtcabinet.data.AppDatabase
import dev.lucasangelo.thoughtcabinet.data.PersonaEntity
import dev.lucasangelo.thoughtcabinet.ui.component.CleanScaffold
import dev.lucasangelo.thoughtcabinet.ui.component.FloatingExtendedTopBar
import dev.lucasangelo.thoughtcabinet.ui.component.FloatingExtendedTopBarActionItem
import dev.lucasangelo.thoughtcabinet.ui.component.FloatingNavigationBar
import dev.lucasangelo.thoughtcabinet.ui.component.FloatingNavigationExpandableItem
import dev.lucasangelo.thoughtcabinet.ui.component.PersonaProfilePicture
import dev.lucasangelo.thoughtcabinet.ui.component.floatingExtendedTopBarPadding
import dev.lucasangelo.thoughtcabinet.ui.component.floatingNavigationBarPadding
import dev.lucasangelo.thoughtcabinet.ui.screen.edit.EditPersonaRoute
import dev.lucasangelo.thoughtcabinet.util.darken
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class InspectPersonaRoute(val id: Long)

@Composable
fun InspectPersonaScreen(
    personaId: Long,
    rootNavController: NavController,
    rootShowSnackbar: (String) -> Unit
) {
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()
    val database = remember { AppDatabase.getInstance(context).dao }

    var currentPersona by remember { mutableStateOf<PersonaEntity?>(null) }

    LaunchedEffect(personaId) {
        currentPersona = database.getPersona(personaId)
    }

    val listState = rememberLazyListState()
    CleanScaffold(
        backgroundColor = Color(currentPersona?.colorTheme ?: 0).darken(),
        topBar = {
            FloatingExtendedTopBar(
                title = currentPersona?.name ?: "",
                description = currentPersona?.description ?: "",
                canGoBack = true,
                onGoBackRequest = { rootNavController.popBackStack() },
                iconContent = { modifier, collapsedFraction ->
                    PersonaProfilePicture(
                        profilePic = currentPersona?.profilePic,
                        modifier = modifier
                            .scale(
                                lerp(.9f, .6f, collapsedFraction)
                            )
                            .clickable(onClick = {
                                coroutineScope
                                    .launch { listState.animateScrollToItem(0) }
                            })
                    )
                },
                actions = listOf(
                    FloatingExtendedTopBarActionItem(
                        name = "Edit",
                        icon = R.drawable.icon_edit,
                        onClick = { rootNavController.navigate(EditPersonaRoute(personaId)) }
                    ),
                    FloatingExtendedTopBarActionItem(
                        name = "Remove",
                        icon = R.drawable.icon_delete,
                        onClick = { }
                    )
                ),
                listState = listState
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
                Text(
                    text = "TODO",
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(vertical = 128.dp)
                        .height(1000.dp)
                        .fillMaxWidth()
                )
            }

            item {
                Spacer(Modifier.height(floatingNavigationBarPadding))
            }
        }

        FloatingNavigationBar(
            tabItems = emptyList(),
            actionItems = listOf(
                FloatingNavigationExpandableItem(
                    icon = R.drawable.icon_add,
                    title = "Add",
                    showTitle = false,
                    items = emptyList()
                )
            )
        )
    }
}