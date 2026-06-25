package dev.lucasangelo.thoughtcabinet.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.lucasangelo.thoughtcabinet.R
import dev.lucasangelo.thoughtcabinet.util.draftsDir
import dev.lucasangelo.thoughtcabinet.util.profilePicDir
import java.io.File

@Composable
fun PersonaProfilePicture(
    profilePic: String?,
    inCache: Boolean = false,
    useBorder: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    if (profilePic == null) {
        Icon(
            painter = painterResource(R.drawable.icon_profile),
            contentDescription = null,
            modifier = modifier
                .fillMaxSize()
        )
    } else {
        AsyncImage(
            model = File(
                if (inCache) context.cacheDir else context.filesDir,
                (if (inCache) draftsDir else profilePicDir) + profilePic
            ),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .fillMaxSize()
                .scale(0.8f)
                .aspectRatio(1f / 1f)
                .then( other =
                    if (useBorder)
                        Modifier.border(
                            border = BorderStroke(width = 1.dp, color = Color.Gray),
                            shape = CircleShape
                        )
                    else
                        Modifier
                )

        )
    }
}
