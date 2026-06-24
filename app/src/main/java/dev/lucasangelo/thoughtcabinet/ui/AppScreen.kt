package dev.lucasangelo.thoughtcabinet.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
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
import dev.lucasangelo.thoughtcabinet.ui.screen.edit.EditPostRoute
import dev.lucasangelo.thoughtcabinet.ui.screen.edit.EditPostScreen
import dev.lucasangelo.thoughtcabinet.ui.screen.inspect.InspectMediaListRoute
import dev.lucasangelo.thoughtcabinet.ui.screen.inspect.InspectMediaListScreen
import dev.lucasangelo.thoughtcabinet.ui.screen.inspect.InspectPersonaRoute
import dev.lucasangelo.thoughtcabinet.ui.screen.inspect.InspectPersonaScreen
import dev.lucasangelo.thoughtcabinet.ui.screen.inspect.InspectPostRoute
import dev.lucasangelo.thoughtcabinet.ui.screen.inspect.InspectPostScreen
import dev.lucasangelo.thoughtcabinet.ui.screen.misc.SearchRoute
import dev.lucasangelo.thoughtcabinet.ui.screen.misc.SearchScreen
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
    val viewModel: AppViewModel = viewModel(
        factory = viewModelFactory {
            initializer { AppViewModel(application.repository) }
        }
    )

    val thoughtsList by viewModel.thoughts.collectAsState()
    val crowdList by viewModel.crowd.collectAsState()
    val thoughtsMap = remember(thoughtsList) { thoughtsList.associateBy { it.id } }
    val crowdMap = remember(crowdList) { crowdList.associateBy { it.id } }

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
            popEnterTransition = {
                EnterTransition.None
            },
            popExitTransition = { slideOutOfContainer(
                    animationSpec = tween(200, easing = EaseIn),
                    towards = AnimatedContentTransitionScope.SlideDirection.End
                ) }
        ) {
            composable<HomeRoute> {
                HomeScreen(
                    navController,
                    thoughtsListState,
                    crowdListState,
                    thoughtsMap,
                    crowdMap,
                )
            }
            composable<SearchRoute> {
                SearchScreen(
                    thoughtsMap,
                    crowdMap,
                    navController
                )
            }

            composable<InspectPostRoute> { backStackEntry ->
                val routeObject :InspectPostRoute = backStackEntry.toRoute()
                InspectPostScreen(
                    routeObject.postId,
                    thoughtsMap,
                    crowdMap,
                    routeObject.requestComment,
                    navController,
                    showSnackbar,
                )
            }
            composable<EditPostRoute> { backStackEntry ->
                val routeObject : EditPostRoute = backStackEntry.toRoute()
                EditPostScreen(
                    routeObject.id,
                    routeObject.repostOf,
                    crowdList,
                    navController,
                    showSnackbar
                )
            }

            composable<InspectPersonaRoute> { backStackEntry ->
                val routeObject :InspectPersonaRoute = backStackEntry.toRoute()
                InspectPersonaScreen(
                    routeObject.personaId,
                    navController,
                    showSnackbar,
                )
            }
            composable<EditPersonaRoute> { backStackEntry ->
                val routeObject :EditPersonaRoute = backStackEntry.toRoute()
                EditPersonaScreen(
                    routeObject.personaId,
                    navController,
                    showSnackbar,
                )
            }
            composable<EditPersonaTraitRoute> { backStackEntry ->
                val routeObject :EditPersonaTraitRoute = backStackEntry.toRoute()
                EditPersonaTraitScreen(
                    routeObject.personaId,
                    routeObject.traitId,
                    navController,
                    showSnackbar,
                )
            }

            composable<InspectMediaListRoute>(
                popExitTransition = {
                    val dismissViaSwipe = initialState.savedStateHandle.get<Boolean>("dismiss_via_swipe") ?: false

                    if (dismissViaSwipe)
                        fadeOut(animationSpec = tween(250))
                    else
                        slideOutOfContainer(
                            animationSpec = tween(200, easing = EaseIn),
                            towards = AnimatedContentTransitionScope.SlideDirection.End
                        )
                }
            ) { backStackEntry ->
                val routeObject : InspectMediaListRoute = backStackEntry.toRoute()
                InspectMediaListScreen(
                    routeObject.list,
                    routeObject.startAt,
                    routeObject.mediaFolder,
                    navController
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