package dev.lucasangelo.thoughtcabinet.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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

// NOTE: inspired by https://github.com/elyesmansour/compose-floating-tab-bar

val floatingNavigationBarPadding = 130.dp

@Composable
fun BoxScope.FloatingNavigationBar(
    tabItems: List<FloatingNavigationActionItem>,
    actionItems: List<FloatingNavigationItem>,
) {
    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black)
                    )
                )
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
            ) {
                tabItems.forEach { item ->
                    FloatingNavigationButton(
                        icon = item.icon,
                        title = item.title,
                        showTitle = item.showTitle,
                        onClick = item.action,
                    )
                }
            }

            Row(
                horizontalArrangement = spacing,
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier
            ) {
                actionItems.forEach { item ->
                    when (item) {
                        is FloatingNavigationActionItem -> {
                            FloatingNavigationButton(
                                icon = item.icon,
                                title = item.title,
                                showTitle = item.showTitle,
                                onClick = item.action,
                            )
                        }
                        is FloatingNavigationExpandableItem -> {
                            var expanded by remember { mutableStateOf(false) }
                            ExpandableFloatingNavigationButton(
                                expanded = expanded,
                                onExpandedChanged = { value -> expanded = value },
                                icon = item.icon,
                                title = item.title,
                                showTitle = item.showTitle,
                            ) {
                                item.items.forEach { subItem ->
                                    FloatingNavigationButton(
                                        icon = subItem.icon,
                                        title = subItem.title,
                                        showTitle = subItem.showTitle,
                                        onClick = {
                                            expanded = false
                                            subItem.action()
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

sealed interface FloatingNavigationItem {
    val icon: Int
    val title: String
    val showTitle: Boolean
}
data class FloatingNavigationActionItem(
    override val icon: Int,
    override val title: String,
    override val showTitle: Boolean,
    val action: () -> Unit,
) : FloatingNavigationItem
data class FloatingNavigationExpandableItem(
    override val icon: Int,
    override val title: String,
    override val showTitle: Boolean,
    val items: List<FloatingNavigationActionItem>,
) : FloatingNavigationItem

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
@Composable
fun ExpandableFloatingNavigationButton(
    expanded: Boolean,
    onExpandedChanged: (Boolean) -> Unit,
    icon: Int,
    title: String,
    showTitle: Boolean,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedVisibility(expanded) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                content()
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable(onClick = { onExpandedChanged(!expanded) }),
        ) {
            val rotation by animateFloatAsState(
                targetValue = if (expanded) 45f else 0f
            )

            Image(
                painter = painterResource(icon),
                contentDescription = title,
                modifier = Modifier
                    .rotate(rotation)
                    .size(72.dp)
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
}