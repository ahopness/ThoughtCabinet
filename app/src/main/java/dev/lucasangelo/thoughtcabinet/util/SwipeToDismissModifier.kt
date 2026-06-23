package dev.lucasangelo.thoughtcabinet.util

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs

fun Modifier.swipeToDismiss(
    onDismiss: () -> Unit,
    onSwipeProgress: (Float) -> Unit = {}
) : Modifier = composed {
    val coroutineScope = rememberCoroutineScope()
    val offsetY = remember { Animatable(0f) }
    val density = LocalDensity.current

    val dismissThresholdPx = remember(density) { with(density) { 150.dp.toPx() } }

    val maxDragDistancePx = dismissThresholdPx * 2f

    return@composed Modifier
        .pointerInput(Unit) {
            detectVerticalDragGestures(
                onDragEnd = {
                    coroutineScope.launch {
                        if (abs(offsetY.value) > dismissThresholdPx) {
                            onDismiss()
                        } else {
                            offsetY.animateTo(0f, spring()) {
                                val progress = (abs(value) / maxDragDistancePx).coerceIn(0f, 1f)
                                onSwipeProgress(progress)
                            }
                        }
                    }
                },
                onDragCancel = {
                    coroutineScope.launch {
                        offsetY.animateTo(0f, spring()) {
                            val progress = (abs(value) / maxDragDistancePx).coerceIn(0f, 1f)
                            onSwipeProgress(progress)
                        }
                    }
                },
                onVerticalDrag = { change, dragAmount ->
                    change.consume()
                    coroutineScope.launch {
                        val newValue = offsetY.value + dragAmount
                        offsetY.snapTo(newValue)
                        val progress = (abs(newValue) / maxDragDistancePx).coerceIn(0f, 1f)
                        onSwipeProgress(progress)
                    }
                }
            )
        }
        .graphicsLayer {
            translationY = offsetY.value
            alpha = (1f - (abs(offsetY.value) / maxDragDistancePx) * 0.85f).coerceIn(0.25f, 1f)
        }
}