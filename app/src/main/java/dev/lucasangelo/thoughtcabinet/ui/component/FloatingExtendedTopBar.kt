package dev.lucasangelo.thoughtcabinet.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import dev.lucasangelo.thoughtcabinet.R
import dev.lucasangelo.thoughtcabinet.util.overlayTransparencyColor
import kotlin.math.roundToInt

val floatingExtendedTopBarPadding = 420.dp

data class FloatingExtendedTopBarActionItem(
    val name: String,
    val icon: Int,
    val onClick: () -> Unit
)

@Composable
fun BoxScope.FloatingExtendedTopBar(
    title: String,
    description: String = "",
    canGoBack: Boolean,
    onGoBackRequest: () -> Unit = { },
    iconContent: @Composable (Modifier, () -> Float) -> Unit,
    actions: List<FloatingExtendedTopBarActionItem>,
    listState: LazyListState,
) {
    var parentSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(overlayTransparencyColor, Color.Transparent)
                )
            )
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
                .layout { measurable, constraints ->
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
            if (canGoBack) {
                Icon(
                    painter = painterResource(R.drawable.icon_back),
                    contentDescription = stringResource(R.string.go_back),
                    modifier = Modifier
                        .size(floatingTopBarButtonSize)
                        .align(Alignment.TopStart)
                        .clickable(onClick = onGoBackRequest)
                )
            }

            iconContent(
                Modifier
                    .size(floatingTopBarButtonSize)
                    .offset {
                        IntOffset(
                            x = lerp(
                                (parentSize.width / 2f) - (floatingTopBarButtonSize / 2f).toPx(),
                                if (canGoBack) floatingTopBarButtonSize.toPx() else 16.dp.toPx(),
                                collapsedFraction.value
                            ).toInt(),
                            y = lerp(
                                (parentSize.height / 2f) - (floatingTopBarButtonSize / 2f).toPx(),
                                0f,
                                collapsedFraction.value
                            ).toInt()
                        )
                    },
                { collapsedFraction.value }
            )

            var titleLineHeight: Float by remember { mutableFloatStateOf(0f) }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset {
                        IntOffset(
                            x = 0,
                            y = lerp(
                                84.dp.toPx(),
                                if (description.isEmpty()) 0f else titleLineHeight,
                                collapsedFraction.value
                            ).toInt()
                        )
                    }
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .alpha(lerp(1f, 0f, collapsedFraction.value)),
                    onTextLayout = {
                        titleLineHeight =
                            it.getLineBottom(0) / maxOf(1, it.lineCount)
                    }
                )
                if (description.isNotEmpty()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .width(360.dp)
                            .alpha(lerp(1f, 0f, collapsedFraction.value))
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy((-24).dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
            ) {
                actions.forEach {
                    Icon(
                        painter = painterResource(it.icon),
                        contentDescription = it.name,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(floatingTopBarButtonSize)
                            .clickable(onClick = it.onClick)
                    )
                }
            }
        }
    }
}
