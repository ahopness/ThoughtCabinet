package dev.lucasangelo.thoughtcabinet.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import dev.lucasangelo.thoughtcabinet.R
import dev.lucasangelo.thoughtcabinet.ui.screen.CrowdRoute
import dev.lucasangelo.thoughtcabinet.ui.screen.ThoughtsRoute

// TODO: make more modular, works for now
@Composable
fun BoxScope.FloatingNavigationBar(
    currentDestination: NavDestination?,
    onNavigate: (route: Any, topLevel: Boolean) -> Unit
) {
    AnimatedContent(
        targetState = (currentDestination?.hasRoute<ThoughtsRoute>() == true ||
                currentDestination?.hasRoute<CrowdRoute>() == true),
        transitionSpec = {
            (slideInVertically(initialOffsetY = { it }) + fadeIn(tween(220)))
                .togetherWith(slideOutVertically(targetOffsetY = { it }) + fadeOut(tween(220)))
        },
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
    ) { toggled ->
        if (!toggled) return@AnimatedContent

        Box(
            modifier = Modifier
                .background(Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black)
                ))
        ) {
            val spacing = Arrangement.spacedBy(8.dp)
            val modifier = Modifier
                .safeDrawingPadding()
                .padding(12.dp)
                .heightIn(92.dp)

            Row(
                horizontalArrangement = spacing,
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier
                    .align(Alignment.BottomStart)
            ) {
                FloatingNavigationButton(
                    icon = R.drawable.icon_list,
                    title = "Thoughts",
                    showTitle = currentDestination?.hasRoute<ThoughtsRoute>() == true,
                    onClick = { onNavigate(ThoughtsRoute, true) },
                )
                FloatingNavigationButton(
                    icon = R.drawable.icon_crowd,
                    title = "Crowd",
                    showTitle = currentDestination?.hasRoute<CrowdRoute>() == true,
                    onClick = { onNavigate(CrowdRoute, true) },
                )
            }

            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier
                    .align(Alignment.BottomEnd)

            ) {
                var newPostOptionsOpen by remember { mutableStateOf(false) }

                AnimatedVisibility(newPostOptionsOpen) {
                    Column(
                        verticalArrangement = spacing,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FloatingNavigationButton(
                            icon = R.drawable.icon_note_post,
                            title = "Note",
                            showTitle = true,
                            onClick = {},
                        )
                        FloatingNavigationButton(
                            icon = R.drawable.icon_article_post,
                            title = "Article",
                            showTitle = true,
                            onClick = {},
                        )
                        FloatingNavigationButton(
                            icon = R.drawable.icon_media_post_alt,
                            title = "Media",
                            showTitle = true,
                            onClick = {},
                        )
                        FloatingNavigationButton(
                            icon = R.drawable.icon_link_post,
                            title = "Link",
                            showTitle = true,
                            onClick = {},
                        )
                    }
                }

                Image(
                    painter = painterResource(R.drawable.icon_add),
                    contentDescription = "Add",
                    modifier = Modifier
                        .size(72.dp)
                        .rotate(if (newPostOptionsOpen) 45f else 0f)
                        .clickable(onClick = { newPostOptionsOpen = !newPostOptionsOpen })
                )
            }
        }
    }
}

@Composable
fun FloatingNavigationButton(
    icon: Int,
    title: String,
    showTitle: Boolean,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = title,
            modifier = Modifier.size(72.dp)
        )
        AnimatedVisibility(showTitle) {
            Text(
                text = title,
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}