package dev.lucasangelo.thoughtcabinet.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.lucasangelo.thoughtcabinet.R

@Composable
fun PagerScaffold(
    title: String,
    backgroundColor: Color = Color.Black,
    canGoBack: Boolean = true,
    onGoBackRequest: () -> Unit,
    pageCount: Int,
    pageContent: @Composable (PagerScope.(Int, PagerState) -> Unit)
) {
    Surface(color = backgroundColor) {
        Box {
            val pagerState = rememberPagerState(pageCount = { pageCount })
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                pageContent = { page -> pageContent(page, pagerState) }
            )

            Box(
                modifier = Modifier
                    .safeDrawingPadding()
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
            ) {
                if (canGoBack) {
                    Image(
                        painter = painterResource(R.drawable.icon_back),
                        contentDescription = "Go Back",
                        modifier = Modifier
                            .size(64.dp)
                            .align(Alignment.CenterStart)
                            .clickable(onClick = onGoBackRequest)
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
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
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(8.dp)
                    )
                }
            }
        }
    }
}