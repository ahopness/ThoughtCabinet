package dev.lucasangelo.thoughtcabinet.ui.screen.misc

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import dev.lucasangelo.thoughtcabinet.ui.component.PagerScaffold
import kotlinx.serialization.Serializable
import androidx.core.net.toUri
import androidx.navigation.NavController
import dev.lucasangelo.thoughtcabinet.R
import dev.lucasangelo.thoughtcabinet.ui.component.PagerScaffoldContent
import dev.lucasangelo.thoughtcabinet.ui.screen.HomeRoute
import dev.lucasangelo.thoughtcabinet.ui.screen.edit.EditPersonaRoute
import io.github.kdroidfilter.composemediaplayer.AudioMode
import io.github.kdroidfilter.composemediaplayer.VideoPlayerSurface
import io.github.kdroidfilter.composemediaplayer.rememberVideoPlayerState
import androidx.core.content.edit

@Serializable
object OnboardingRoute

@Composable
fun OnboardingScreen(
    rootNavController: NavController
) {
    val context = LocalContext.current

    val sharedPrefs = remember {
        context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    }
    val showOnboarding = remember {
        sharedPrefs.getBoolean("show_onboarding", true)
    }

    PagerScaffold(
        title = "",
        canGoBack = !showOnboarding,
        onGoBackRequest = { rootNavController.popBackStack() },
        pageCount = 4,
        prelude = {
            val context = LocalContext.current
            val videoUri = "android.resource://${context.packageName}/${R.raw.background}"

            val playerState = rememberVideoPlayerState()
            LaunchedEffect(Unit) {
                playerState.volume = 0f
                playerState.loop = true
                playerState.openUri(videoUri)
            }

            VideoPlayerSurface(
                playerState = playerState,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.5f)
            )

        }
    ) { pagerState, page, offsetDistance, onNextPageRequested ->
        listOf(
            {
                PagerScaffoldContent(offsetDistance) {
                    Text(
                        text = "Welcome to",
                        modifier = Modifier.offset(y = (50).dp)
                    )
                    Image(
                        painter = painterResource(R.drawable.logo),
                        contentDescription = "Thought Cabinet",
                        modifier = Modifier.size(250.dp)
                    )
                }
            },
            {
                PagerScaffoldContent(offsetDistance) {
                    Text(
                        buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("Thought Cabinet")
                            }
                            append(" is Your private microblogger.")
                        },
                        textAlign = TextAlign.Center
                    )

                    Text("This is a diary disguised as a feed, you can freely post your thoughts here without the fear of other’s reactions and algorithms feeding off of you.")

//                    Button(onClick = onNextPageRequested) {
//                        Text("Next")
//                    }
                }
            },
            {
                PagerScaffoldContent(offsetDistance) {
                    Text(
                        buildAnnotatedString {
                            append("In Thought Cabinet, instead of users, posts are made by ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("personas")
                            }
                            append(".")
                        },
                        textAlign = TextAlign.Center
                    )

                    Text("Personas are the social role that one adopts: They can be things we aspire to be, specific personalities or even fictional characters.")
                    Text("Instead of a profile page, personas have a board which you can add personality traits to, don't forget to do that after you're done here!")

                    Button(onClick = {
                        rootNavController.navigate(EditPersonaRoute(null))
                        onNextPageRequested()
                    }) {
                        Text("Create Your First Persona")
                    }
                }
            },
            {
                PagerScaffoldContent(offsetDistance) {
                    Text("Instead of starting with a black canvas, it is recommended to import posts from other social media and personal diaries, this helps makes it easier to get incentivised to post something everytime you open the app.")

                    Text("Now that everything is setup, you can start your experience, enjoy :)")

                    Button(onClick = {
                        if (showOnboarding) {
                            sharedPrefs.edit { putBoolean("show_onboarding", false) }

                            rootNavController.navigate(HomeRoute) {
                                popUpTo(OnboardingRoute) { inclusive = true }
                            }
                        } else {
                            rootNavController.popBackStack()
                        }
                    }) {
                        Text("Enter Thought Cabinet")
                    }
                }
            }
        )
    }
}