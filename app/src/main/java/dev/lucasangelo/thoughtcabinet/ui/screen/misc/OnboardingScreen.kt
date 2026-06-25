package dev.lucasangelo.thoughtcabinet.ui.screen.misc

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.navigation.NavController
import dev.lucasangelo.thoughtcabinet.R
import dev.lucasangelo.thoughtcabinet.ui.component.PagerScaffold
import dev.lucasangelo.thoughtcabinet.ui.component.PagerScaffoldContent
import dev.lucasangelo.thoughtcabinet.ui.screen.HomeRoute
import dev.lucasangelo.thoughtcabinet.ui.screen.edit.EditPersonaRoute
import io.github.kdroidfilter.composemediaplayer.AudioMode
import io.github.kdroidfilter.composemediaplayer.InterruptionMode
import io.github.kdroidfilter.composemediaplayer.VideoPlayerSurface
import io.github.kdroidfilter.composemediaplayer.rememberVideoPlayerState
import kotlinx.serialization.Serializable

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

            val playerState = rememberVideoPlayerState( audioMode = AudioMode(
                    interruptionMode = InterruptionMode.MixWithOthers
            ) )
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
                        text = stringResource(R.string.onboarding_welcome),
                        modifier = Modifier.offset(y = (50).dp)
                    )
                    Image(
                        painter = painterResource(R.drawable.logo),
                        contentDescription = stringResource(R.string.app_name),
                        modifier = Modifier.size(250.dp)
                    )
                }
            },
            {
                PagerScaffoldContent(offsetDistance) {
                    Text(
                        buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(stringResource(R.string.app_name))
                            }
                            append(' ')
                            append(stringResource(R.string.onboarding_desc_suffix))
                        },
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(stringResource(R.string.onboarding_description))
                }
            },
            {
                PagerScaffoldContent(offsetDistance) {
                    Text(
                        buildAnnotatedString {
                            append(stringResource(R.string.onboarding_made_by_prefix))
                            append(' ')
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(stringResource(R.string.onboarding_personas_word))
                            }
                            append(".")
                        },
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(stringResource(R.string.onboarding_personas_description))
                    Text(stringResource(R.string.onboarding_personas_board_hint))

                    Button(onClick = {
                        rootNavController.navigate(EditPersonaRoute(null))
                        onNextPageRequested()
                    }) {
                        if (showOnboarding)
                            Text(stringResource(R.string.onboarding_create_first_persona))
                        else
                            Text(stringResource(R.string.create_a_persona))
                    }
                }
            },
            {
                PagerScaffoldContent(offsetDistance) {
                    Text(stringResource(R.string.onboarding_import_recommendation))

                    Text(stringResource(R.string.onboarding_setup_done))

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
                        Text(stringResource(R.string.onboarding_enter_app))
                    }
                }
            }
        )
    }
}