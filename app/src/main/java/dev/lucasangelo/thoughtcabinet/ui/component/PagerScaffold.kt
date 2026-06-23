package dev.lucasangelo.thoughtcabinet.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun PagerScaffold(
    title: String,
    backgroundColor: Color = Color.Black,
    canGoBack: Boolean = true,
    onGoBackRequest: () -> Unit,
    pageCount: Int,
    initialPage: Int = 0,
    pageContent:
        @Composable PagerScope.(
            PagerState, Int, Float, () -> Unit
        ) -> List<@Composable () -> Unit>
) {
    CleanScaffold(
        backgroundColor = backgroundColor,
        topBar = {
            FloatingTopBar(
                title = title,
                canGoBack = canGoBack,
                onGoBackRequest = onGoBackRequest
            )
        }
    ) {
        Box {
            val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { pageCount })
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                val offsetDistance = { pagerState.getOffsetDistanceInPages(page) }

                val coroutineScope = rememberCoroutineScope()
                val onNextPageRequested: () -> Unit =
                    { coroutineScope.launch { pagerState.animateScrollToPage(page+1) } }

                pageContent(
                    pagerState, page, offsetDistance(), onNextPageRequested
                )[page]()
            }

            if (pageCount > 1)
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .background(Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(0.5f))
                        ))
                        .wrapContentHeight()
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .safeDrawingPadding()
                        .padding(bottom = 32.dp),
                ) {
                    repeat(pagerState.pageCount) { iteration ->
                        val color = if (pagerState.currentPage == iteration) Color.LightGray else Color.DarkGray
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(8.dp)
                        )
                    }
                }
        }
    }
}

@Composable
fun PagerScaffoldContent(
    pageOffsetDistance: Float,
    modifier: Modifier = Modifier,
    defaultSpacing: Dp = 32.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier.fillMaxSize()) {
        val distance = pageOffsetDistance.coerceIn(0f, 1f)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(defaultSpacing),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(defaultSpacing)
                .graphicsLayer {
                    translationY = 200 * distance
                    alpha = 1f * (1f - distance)
                },
            content = content
        )
    }
}