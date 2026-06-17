package dev.lucasangelo.thoughtcabinet.util

import androidx.compose.ui.graphics.Color

inline fun Color.darken(darkenBy: Float = 0.25f): Color {
    return copy(
        red = red * darkenBy,
        green = green * darkenBy,
        blue = blue * darkenBy,
        alpha = alpha
    )
}
