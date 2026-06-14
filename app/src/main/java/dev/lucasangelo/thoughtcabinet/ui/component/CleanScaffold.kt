package dev.lucasangelo.thoughtcabinet.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlin.math.roundToInt

@Composable
fun CleanScaffold(
    title: String,
    icon: Int,
    topBarActionName: String,
    topBarActionIcon: Int,
    onTopBarActionClicked: () -> Unit,
    listState : LazyListState,
    content: @Composable (topBarSpacing: Dp, navBarSpacing: Dp) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        val topBarHeight = 420.dp
        val collapseRangePx = with(LocalDensity.current) { topBarHeight.toPx() }

        val collapseFraction = remember(collapseRangePx) {
            derivedStateOf {
                when {
                    listState.firstVisibleItemIndex > 0 -> 1f
                    else -> (listState.firstVisibleItemScrollOffset / collapseRangePx).coerceIn(0f, 1f)
                }
            }
        }

        content(topBarHeight, 128.dp)

        FloatingTopBar(
            title = title,
            icon = icon,
            actionName = topBarActionName,
            actionIcon = topBarActionIcon,
            onActionClick = onTopBarActionClicked,
            collapsedFraction = collapseFraction,
            maxHeight = topBarHeight,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}