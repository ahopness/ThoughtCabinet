package dev.lucasangelo.thoughtcabinet.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CleanScaffold(
    title: String,
    icon: Int,
    topBarActionName: String,
    topBarActionIcon: Int,
    onTopBarActionClicked: () -> Unit,
    listState : LazyListState,
    content: @Composable (topBarSpacing: Dp) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        val topBarHeight = 512.dp
        val collapseRangePx = with(LocalDensity.current) {
            topBarHeight.toPx()
        }

        val collapseFraction = remember {
            derivedStateOf {
                when {
                    listState.firstVisibleItemIndex > 0 -> 1f
                    else -> (listState.firstVisibleItemScrollOffset / collapseRangePx).coerceIn(0f, 1f)
                }
            }
        }

        CollapsingTopBar(
            title = title,
            icon = icon,
            actionName = topBarActionName,
            actionIcon = topBarActionIcon,
            onActionClick = onTopBarActionClicked,
            collapsedFraction = collapseFraction,
            maxHeight = topBarHeight,
        )

        content(topBarHeight)
    }
}

@Composable
fun BoxScope.CollapsingTopBar(
    title: String,
    icon: Int,
    actionName: String,
    actionIcon: Int,
    onActionClick: () -> Unit,
    collapsedFraction: State<Float>,
    maxHeight: Dp,
) {
    var parentSize by remember { mutableStateOf(IntSize.Zero) }

    val height = lerp(
        maxHeight,
        72.dp,
        collapsedFraction.value
    )

    Box(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .zIndex(1f) // NOTE: background doesn't render if z isn't set, no idea why
            .background(Brush.verticalGradient(
                colors = listOf(Color.Black, Color.Transparent)
            ))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .safeDrawingPadding()
                .height(height)
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
                    .size(72.dp)
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
                    .offset(
                        y = lerp(
                            72.dp,
                            0.dp,
                            collapsedFraction.value
                        )
                    )
            )

            Image(
                painter = painterResource(actionIcon),
                contentDescription = actionName,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(72.dp)
                    .align(Alignment.BottomEnd)
                    .clickable(onClick = onActionClick)
            )
        }
    }
}