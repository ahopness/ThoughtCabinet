package dev.lucasangelo.thoughtcabinet.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlin.math.roundToInt

val floatingExtendedTopBarPadding = 420.dp

@Composable
fun BoxScope.FloatingExtendedTopBar(
    title: String,
    icon: Int,
    onIconClicked: () -> Unit,
    actionName: String,
    actionIcon: Int,
    onActionClick: () -> Unit,
    listState: LazyListState,
) {
    var parentSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
            .background(Brush.verticalGradient(
                colors = listOf(Color.Black, Color.Transparent)
            ))
    ) {
        val collapseRangePx = with(LocalDensity.current) { floatingExtendedTopBarPadding.toPx() / 1.5f }
        val collapsedFraction = remember(collapseRangePx) {
            derivedStateOf {
                when {
                    (listState.firstVisibleItemIndex > 0) -> 1f
                    else -> (listState.firstVisibleItemScrollOffset / collapseRangePx).coerceIn(0f, 1f)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .safeDrawingPadding()
                .layout{ measurable, constraints ->
                    val currentHeight = lerp(
                        floatingExtendedTopBarPadding.toPx(),
                        floatingTopBarButtonSize.toPx(),
                        collapsedFraction.value
                    ).roundToInt()

                    val placeable = measurable.measure(
                        constraints.copy(minHeight = currentHeight, maxHeight = currentHeight)
                    )
                    layout(placeable.width, placeable.height) {
                        placeable.placeRelative(0, 0)
                    }
                }
                .onSizeChanged {
                    parentSize = it
                }
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = title,
                contentScale = ContentScale.Inside,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(floatingTopBarButtonSize)
                    .clickable(onClick = onIconClicked)
                    .offset {
                        IntOffset(
                            x = lerp(
                                (parentSize.width / 2f) - 36.dp.toPx(),
                                16.dp.toPx(),
                                collapsedFraction.value
                            ).toInt(),
                            y = lerp(
                                parentSize.height / 2f - 36.dp.toPx(),
                                0f,
                                collapsedFraction.value
                            ).toInt()
                        )
                    }
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset {
                        IntOffset(
                            x = 0,
                            y = lerp(
                                72.dp.toPx(),
                                0f,
                                collapsedFraction.value
                            ).toInt()
                        )
                    }
            )

            Image(
                painter = painterResource(actionIcon),
                contentDescription = actionName,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(floatingTopBarButtonSize)
                    .align(Alignment.BottomEnd)
                    .clickable(onClick = onActionClick)
            )
        }
    }
}