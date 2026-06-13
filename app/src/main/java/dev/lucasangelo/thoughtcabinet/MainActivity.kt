package dev.lucasangelo.thoughtcabinet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.lucasangelo.thoughtcabinet.components.FloatingNavigationBar
import dev.lucasangelo.thoughtcabinet.screens.CrowdScreen
import dev.lucasangelo.thoughtcabinet.screens.HomeScreen
import dev.lucasangelo.thoughtcabinet.screens.PersonaScreen
import dev.lucasangelo.thoughtcabinet.screens.PostScreen
import dev.lucasangelo.thoughtcabinet.screens.SettingsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ThoughtCabinetTheme {
                App()
            }
        }
    }
}

//@Preview
@Composable
fun App() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        NavHost(
            navController = navController,
            startDestination = HomeScreen,
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
            composable<HomeScreen>(
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None }
            ) {
                HomeScreen()
            }
            composable<PostScreen> {
                PostScreen()
            }

            composable<CrowdScreen>(
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None }
            ) {
                CrowdScreen()
            }
            composable<PersonaScreen> {
                PersonaScreen()
            }

            composable<SettingsScreen>(
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None }
            ) {
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

