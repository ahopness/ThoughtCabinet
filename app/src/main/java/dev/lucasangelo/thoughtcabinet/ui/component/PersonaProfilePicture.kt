package dev.lucasangelo.thoughtcabinet.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import dev.lucasangelo.thoughtcabinet.R
import java.io.File

@Composable
fun PersonaProfilePicture(
    profilePic: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    if (profilePic == null) {
        Image(
            painter = painterResource(R.drawable.icon_profile),
            contentDescription = null,
            modifier = modifier.fillMaxSize()
        )
    } else {
        AsyncImage(
            model = File(context.filesDir, "profile-pictures/${profilePic}"),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .fillMaxSize()
                .clip(CircleShape)
                .aspectRatio(1f / 1f)
        )
    }
}