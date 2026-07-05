package dev.lucasangelo.thoughtcabinet.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import dev.lucasangelo.thoughtcabinet.R
import dev.lucasangelo.thoughtcabinet.data.PersonaEntity
import dev.lucasangelo.thoughtcabinet.data.PostEntity
import dev.lucasangelo.thoughtcabinet.ui.component.FloatingNavigationActionItem
import dev.lucasangelo.thoughtcabinet.ui.component.FloatingNavigationBar
import dev.lucasangelo.thoughtcabinet.ui.component.FloatingNavigationExpandableItem
import dev.lucasangelo.thoughtcabinet.ui.screen.edit.EditPersonaRoute
import dev.lucasangelo.thoughtcabinet.ui.screen.edit.EditPostRoute
import dev.lucasangelo.thoughtcabinet.ui.screen.home.CrowdScreen
import dev.lucasangelo.thoughtcabinet.ui.screen.home.ThoughtsScreen
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Composable
fun HomeScreen(
    thoughtsListState: LazyListState,
    crowdListState: LazyListState,
    thoughts: Map<Long, PostEntity>,
    crowd: Map<Long, PersonaEntity>,
    rootNavController: NavController,
    rootShowSnackbar: (String) -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        val pagerState = rememberPagerState(pageCount = { 2 })

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when(page) {
                0 -> ThoughtsScreen(thoughtsListState, thoughts, crowd, rootNavController, rootShowSnackbar)
                1 -> CrowdScreen(crowdListState, crowd, rootNavController)
            }
        }

        val coroutineScope = rememberCoroutineScope()
        FloatingNavigationBar(
            customOverlayTransparencyColor = Color.Black,
            tabItems = listOf(
                FloatingNavigationActionItem(
                    icon = R.drawable.icon_list,
                    title = stringResource(R.string.thoughts),
                    showTitle = pagerState.currentPage == 0,
                    action = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                ),
                FloatingNavigationActionItem(
                    icon = R.drawable.icon_crowd,
                    title = stringResource(R.string.crowd),
                    showTitle = pagerState.currentPage == 1,
                    action = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                ),
            ),
            actionItems = listOf(
                FloatingNavigationExpandableItem(
                    icon = R.drawable.icon_add,
                    title = stringResource(R.string.add),
                    showTitle = false,
                    items = listOf(
                        FloatingNavigationActionItem(
                            icon = R.drawable.icon_post,
                            title = stringResource(R.string.post),
                            showTitle = true,
                            action = { rootNavController.navigate(EditPostRoute(null)) }
                        ),
                        FloatingNavigationActionItem(
                            icon = R.drawable.icon_persona,
                            title = stringResource(R.string.persona),
                            showTitle = true,
                            action = { rootNavController.navigate(EditPersonaRoute(null)) }
                        ),
                    )
                )
            )
        )
    }
}