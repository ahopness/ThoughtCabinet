package dev.lucasangelo.thoughtcabinet.ui.screen

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.lucasangelo.thoughtcabinet.R
import dev.lucasangelo.thoughtcabinet.ui.component.FloatingNavigationActionItem
import dev.lucasangelo.thoughtcabinet.ui.component.FloatingNavigationBar
import dev.lucasangelo.thoughtcabinet.ui.component.FloatingNavigationExpandableItem
import dev.lucasangelo.thoughtcabinet.ui.component.FloatingNavigationRouteItem
import dev.lucasangelo.thoughtcabinet.ui.screen.edit.EditPersonaRoute
import dev.lucasangelo.thoughtcabinet.ui.screen.home.CrowdRoute
import dev.lucasangelo.thoughtcabinet.ui.screen.home.CrowdScreen
import dev.lucasangelo.thoughtcabinet.ui.screen.home.ThoughtsRoute
import dev.lucasangelo.thoughtcabinet.ui.screen.home.ThoughtsScreen
import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Composable
fun HomeScreen(rootNavController: NavController) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Box(Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = ThoughtsRoute,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            composable<ThoughtsRoute>() {
                ThoughtsScreen(rootNavController)
            }
            composable<CrowdRoute>() {
                CrowdScreen(rootNavController)
            }
        }

        FloatingNavigationBar(
            currentDestination = currentDestination,
            onNavigate = remember(navController) {
                { route: Any, topLevel: Boolean ->
                    navController.navigate(route) {
                        if (topLevel) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            },
            tabItems = listOf(
                FloatingNavigationRouteItem(
                    icon = R.drawable.icon_list,
                    title = "Thoughts",
                    route = ThoughtsRoute
                ),
                FloatingNavigationRouteItem(
                    icon = R.drawable.icon_crowd,
                    title = "Crowd",
                    route = CrowdRoute
                ),
            ),
            actionItems =
                if (currentDestination?.hasRoute<ThoughtsRoute>() == true ||
                    currentDestination?.hasRoute<CrowdRoute>() == true)
                    listOf(
                        FloatingNavigationExpandableItem(
                            icon = R.drawable.icon_add,
                            title = "Add",
                            items =
                                if(currentDestination.hasRoute<ThoughtsRoute>())
                                    listOf(
                                        FloatingNavigationActionItem(
                                            icon = R.drawable.icon_note_post,
                                            title = "Crowd",
                                            action = {}
                                        ),
                                        FloatingNavigationActionItem(
                                            icon = R.drawable.icon_article_post,
                                            title = "Article",
                                            action = {}
                                        ),
                                        FloatingNavigationActionItem(
                                            icon = R.drawable.icon_media_post_alt,
                                            title = "Media",
                                            action = {}
                                        ),
                                        FloatingNavigationActionItem(
                                            icon = R.drawable.icon_link_post,
                                            title = "Link",
                                            action = {}
                                        ),
                                    )
                                else
                                    listOf(
                                        FloatingNavigationActionItem(
                                            icon = R.drawable.icon_persona,
                                            title = "Persona",
                                            action = { rootNavController.navigate(EditPersonaRoute(null)) }
                                        ),
                                    )
                        )
                    )
                else
                    emptyList()
        )
    }
}