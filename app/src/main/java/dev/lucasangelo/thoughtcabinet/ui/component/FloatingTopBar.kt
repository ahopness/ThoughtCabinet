package dev.lucasangelo.thoughtcabinet.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.lucasangelo.thoughtcabinet.R

val floatingTopBarPadding = 120.dp
val floatingTopBarButtonSize = 72.dp

@Composable
fun BoxScope.FloatingTopBar(
    title: String,
    canGoBack: Boolean,
    onGoBackRequest: () -> Unit,
) {
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
                    .size(floatingTopBarButtonSize)
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
}