package dev.lucasangelo.thoughtcabinet.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.lucasangelo.thoughtcabinet.ui.component.FloatingNavigationBar
import dev.lucasangelo.thoughtcabinet.ui.screen.CrowdScreen
import dev.lucasangelo.thoughtcabinet.ui.screen.CrowdRoute
import dev.lucasangelo.thoughtcabinet.ui.screen.PersonaScreen
import dev.lucasangelo.thoughtcabinet.ui.screen.PersonaRoute
import dev.lucasangelo.thoughtcabinet.ui.screen.PostScreen
import dev.lucasangelo.thoughtcabinet.ui.screen.PostRoute
import dev.lucasangelo.thoughtcabinet.ui.screen.SettingsScreen
import dev.lucasangelo.thoughtcabinet.ui.screen.SettingsRoute
import dev.lucasangelo.thoughtcabinet.ui.screen.ThoughtsScreen
import dev.lucasangelo.thoughtcabinet.ui.screen.ThoughtsRoute

@Composable
fun App() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = ThoughtsRoute,
            enterTransition = {
                slideIntoContainer(
                    animationSpec = tween(300, easing = EaseIn),
                    towards = AnimatedContentTransitionScope.SlideDirection.Start
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    animationSpec = tween(300, easing = EaseOut),
                    towards = AnimatedContentTransitionScope.SlideDirection.End
                )
            }
        ) {
            composable<ThoughtsRoute>(
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None }
            ) {
                ThoughtsScreen()
            }
            composable<PostRoute> {
                PostScreen()
            }

            composable<CrowdRoute>(
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None }
            ) {
                CrowdScreen()
            }
            composable<PersonaRoute> {
                PersonaScreen()
            }

            composable<SettingsRoute> {
                SettingsScreen()
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
            }
        )
    }
}