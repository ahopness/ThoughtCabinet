package dev.lucasangelo.thoughtcabinet.ui.screen.misc

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import dev.lucasangelo.thoughtcabinet.R
import dev.lucasangelo.thoughtcabinet.data.CommentEntity
import dev.lucasangelo.thoughtcabinet.data.PersonaEntity
import dev.lucasangelo.thoughtcabinet.data.PostEntity
import dev.lucasangelo.thoughtcabinet.ui.component.PagerScaffold
import dev.lucasangelo.thoughtcabinet.ui.component.PagerScaffoldContent
import dev.lucasangelo.thoughtcabinet.ui.component.PersonaProfilePicture
import dev.lucasangelo.thoughtcabinet.ui.component.pagerScaffoldContentSpacing
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.temporal.ChronoUnit

@Serializable
object StatsRoute

@Composable
fun StatsScreen(
    thoughts: Map<Long, PostEntity>,
    comments: Map<Long, CommentEntity>,
    crowd: Map<Long, PersonaEntity>,
    rootNavController: NavController
) {
    PagerScaffold(
        title = stringResource(R.string.statistics),
        canGoBack = true,
        onGoBackRequest = { rootNavController.popBackStack() },
        pageCount = 2,
        prelude = {
            Box(Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier
                        .alpha(0.5f)
                        .size(512.dp)
                        .align(Alignment.BottomStart)
                        .offset(x = (-128).dp)
                )
            }
        }
    ) { pagerState, page, offsetDistance, onNextPageRequested ->
        listOf(
            {
                PagerScaffoldContent(offsetDistance) {
                    Text(
                        text = stringResource(R.string.stats_total_intro),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(24.dp))

                    // NOTE: theres prob an easier way to do this... dont know how tho
                    val postLabel = stringResource(R.string.stats_posts)
                    val commentsLabel = stringResource(R.string.stats_comments)
                    val bookmarksLabel = stringResource(R.string.stats_bookmarks)
                    val personaLabel = stringResource(R.string.stats_personas)
                    val repostLabel = stringResource(R.string.stats_reposts)
                    val likeLabel = stringResource(R.string.stats_likes)
                    val traitLabel = stringResource(R.string.stats_traits)
                    val daysLabel = stringResource(R.string.stats_days_posting)

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(pagerScaffoldContentSpacing),
                        verticalArrangement = Arrangement.spacedBy(pagerScaffoldContentSpacing),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val firstPostDate = thoughts.values.minOfOrNull { it.createdAt }
                        val daysSinceFirstPost = firstPostDate?.let {
                            ChronoUnit.DAYS.between(it, Instant.now()).coerceAtLeast(0)
                        } ?: 0

                        items(
                            listOf(
                                postLabel to thoughts.size.toString(),
                                personaLabel to crowd.size.toString(),
                                repostLabel to thoughts.values.count { it.repostOf != null }.toString(),
                                likeLabel to thoughts.values.count { it.liked }.toString(),
                                traitLabel to crowd.values.sumOf { it.traits.size }.toString(),
                                commentsLabel to comments.values.count().toString(),
                                bookmarksLabel to thoughts.values.count { it.bookmarked }.toString(),
                                daysLabel to daysSinceFirstPost.toString()
                            )
                        ) { (label, value) ->
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = value, fontSize = 64.sp)
                                Text(text = label, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            },
            {
                PagerScaffoldContent(offsetDistance) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(pagerScaffoldContentSpacing)
                    ) {
                        items(crowd.values.toList(), key = { it.id }) { persona ->
                            Box(
                                modifier = Modifier
                                    .background(Color(persona.colorTheme))
                                    .clip(RoundedCornerShape(6.dp))
                                    .border(
                                        border = BorderStroke(width = 1.dp, color = Color.Gray),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Absolute.Left
                                ) {
                                    PersonaProfilePicture(
                                        persona.profilePic,
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .size(120.dp)
                                    )
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                        horizontalAlignment = Alignment.End,
                                        modifier = Modifier
                                            .padding(12.dp)
                                            .fillMaxWidth()
                                    ) {
                                        Text(
                                            text = persona.name,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(Modifier.height(6.dp))

                                        val posts = thoughts.values.filter { it.authorId == persona.id }
                                        Text(
                                            text = stringResource(
                                                R.string.stats_persona_summary,
                                                posts.size,
                                                posts.count { it.repostOf != null },
                                                comments.values.count { it.authorId == persona.id },
                                                posts.count { it.liked },
                                                posts.count { it.bookmarked },
                                                persona.traits.size
                                            ),
                                            textAlign = TextAlign.End
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
        )
    }
}
