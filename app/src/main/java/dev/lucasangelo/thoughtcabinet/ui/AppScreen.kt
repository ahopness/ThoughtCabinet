package dev.lucasangelo.thoughtcabinet.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dev.lucasangelo.thoughtcabinet.MainApplication
import dev.lucasangelo.thoughtcabinet.ui.screen.HomeRoute
import dev.lucasangelo.thoughtcabinet.ui.screen.HomeScreen
import dev.lucasangelo.thoughtcabinet.ui.screen.edit.EditPersonaRoute
import dev.lucasangelo.thoughtcabinet.ui.screen.edit.EditPersonaScreen
import dev.lucasangelo.thoughtcabinet.ui.screen.edit.EditPersonaTraitRoute
import dev.lucasangelo.thoughtcabinet.ui.screen.edit.EditPersonaTraitScreen
import dev.lucasangelo.thoughtcabinet.ui.screen.inspect.InspectPersonaRoute
import dev.lucasangelo.thoughtcabinet.ui.screen.inspect.InspectPersonaScreen
import kotlinx.coroutines.launch

@Composable
fun AppScreen() {
    val navController = rememberNavController()

    val snackbarScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val showSnackbar: (String) -> Unit = { message: String ->
        snackbarScope.launch { snackbarHostState.showSnackbar(message) }
    }

    val context = LocalContext.current

    val application = context.applicationContext as MainApplication
    val database = application.database
    val viewModel: AppViewModel = viewModel(
        factory = viewModelFactory {
            initializer { AppViewModel(dao = database.dao) }
        }
    )

    val thoughts by viewModel.thoughts.collectAsState()
    val crowd by viewModel.crowd.collectAsState()

    val thoughtsListState = rememberLazyListState()
    val crowdListState = rememberLazyListState()

    Box {
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            enterTransition = { slideIntoContainer(
                    animationSpec = tween(200, easing = EaseOut),
                    towards = AnimatedContentTransitionScope.SlideDirection.Start
                ) },
            exitTransition = { slideOutOfContainer(
                    animationSpec = tween(200, easing = EaseIn),
                    towards = AnimatedContentTransitionScope.SlideDirection.End
                ) }
        ) {
            composable<HomeRoute>(
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None }
            ) {
                HomeScreen(
                    navController,
                    thoughtsListState,
                    crowdListState,
                    thoughts,
                    crowd,
                )
            }

            composable<InspectPersonaRoute>(
                enterTransition =
                    { if (!initialState.destination.hasRoute<HomeRoute>()) EnterTransition.None else null },
                exitTransition =
                    { if (!targetState.destination.hasRoute<HomeRoute>()) ExitTransition.None else null }
            ) { backStackEntry ->
                val routeObject :InspectPersonaRoute = backStackEntry.toRoute()
                InspectPersonaScreen(
                    routeObject.personaId,
                    navController,
                    showSnackbar,
                )
            }
            composable<EditPersonaRoute>() { backStackEntry ->
                val routeObject :EditPersonaRoute = backStackEntry.toRoute()
                EditPersonaScreen(
                    routeObject.personaId,
                    navController,
                    showSnackbar,
                )
            }
            composable<EditPersonaTraitRoute>() { backStackEntry ->
                val routeObject :EditPersonaTraitRoute = backStackEntry.toRoute()
                EditPersonaTraitScreen(
                    routeObject.personaId,
                    routeObject.traitId,
                    navController,
                    showSnackbar,
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .safeDrawingPadding()
        )
    }
}