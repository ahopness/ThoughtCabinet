package dev.lucasangelo.thoughtcabinet.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun CleanScaffold(
    backgroundColor: Color = Color.Black,
    topBar: @Composable BoxScope.() -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    Surface(
        color = backgroundColor,
        modifier = Modifier.fillMaxSize()
    ) {
        Box {
            content()
            topBar()
        }
    }
}
