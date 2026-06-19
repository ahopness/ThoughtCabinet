package dev.lucasangelo.thoughtcabinet.ui.screen.inspect

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.navigation.NavController
import androidx.navigation.Navigator
import androidx.navigation.navOptions
import dev.lucasangelo.thoughtcabinet.R
import dev.lucasangelo.thoughtcabinet.data.AppDao
import dev.lucasangelo.thoughtcabinet.data.AppDatabase
import dev.lucasangelo.thoughtcabinet.data.PersonaEntity
import dev.lucasangelo.thoughtcabinet.ui.component.CleanScaffold
import dev.lucasangelo.thoughtcabinet.ui.component.FloatingExtendedTopBar
import dev.lucasangelo.thoughtcabinet.ui.component.FloatingExtendedTopBarActionItem
import dev.lucasangelo.thoughtcabinet.ui.component.FloatingNavigationActionItem
import dev.lucasangelo.thoughtcabinet.ui.component.FloatingNavigationBar
import dev.lucasangelo.thoughtcabinet.ui.component.FloatingNavigationExpandableItem
import dev.lucasangelo.thoughtcabinet.ui.component.PersonaProfilePicture
import dev.lucasangelo.thoughtcabinet.ui.component.floatingExtendedTopBarPadding
import dev.lucasangelo.thoughtcabinet.ui.component.floatingNavigationBarPadding
import dev.lucasangelo.thoughtcabinet.ui.screen.edit.EditPersonaRoute
import dev.lucasangelo.thoughtcabinet.ui.screen.edit.EditPersonaTraitRoute
import dev.lucasangelo.thoughtcabinet.util.darken
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class InspectPersonaRoute(val id: Long)

@Composable
fun InspectPersonaScreen(
    personaId: Long,
    rootNavController: NavController,
    rootShowSnackbar: (String) -> Unit,
    database: AppDao,
) {
    val coroutineScope = rememberCoroutineScope()

    var currentPersona by remember { mutableStateOf<PersonaEntity?>(null) }
    LaunchedEffect(personaId) {
        currentPersona = database.getPersona(personaId)
    }

    var openDeleteAlertDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    CleanScaffold(
        backgroundColor = Color(currentPersona?.colorTheme ?: 0).darken(),
        topBar = {
            FloatingExtendedTopBar(
                title = currentPersona?.name ?: "",
                description = currentPersona?.bio ?: "",
                canGoBack = true,
                onGoBackRequest = { rootNavController.popBackStack() },
                iconContent = { modifier, collapsedFraction ->
                    PersonaProfilePicture(
                        profilePic = currentPersona?.profilePic,
                        modifier = modifier
                            .scale(
                                lerp(.9f, .6f, collapsedFraction())
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
                        onClick = { openDeleteAlertDialog = true }
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
                    items = listOf(
                        FloatingNavigationActionItem(
                            icon = R.drawable.icon_post,
                            title = "Trait",
                            showTitle = true,
                            action = { rootNavController.navigate(EditPersonaTraitRoute(personaId)) }
                        ),
                    )
                )
            )
        )
    }

    if (openDeleteAlertDialog) {
        val onDismissRequest: () -> Unit = { openDeleteAlertDialog = false }
        AlertDialog(
            containerColor = Color.Black,
            icon = { Image(
                painter = painterResource(R.drawable.icon_delete),
                contentDescription = null,
                modifier = Modifier.size(64.dp)
            )
            },
            title = { Text("Are you sure?") },
            text = { Text("If you delete this persona, you can't recover it later.") },
            onDismissRequest = onDismissRequest,
            dismissButton = {
                Button(onClick = onDismissRequest) {
                    Text("Dismiss")
                }
            },
            confirmButton = {
                Button(onClick = {
                    coroutineScope.launch {
                        currentPersona?.let { database.deletePersona(it) }
                        onDismissRequest()
                        rootNavController.popBackStack()
                        rootShowSnackbar("Persona deletes successfully!")
                    }
                }) {
                    Text("Confirm")
                }
            },
        )
    }
}