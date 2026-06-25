package dev.lucasangelo.thoughtcabinet.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.lucasangelo.thoughtcabinet.R
import dev.lucasangelo.thoughtcabinet.data.PersonaEntity
import dev.lucasangelo.thoughtcabinet.util.darken
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthorSelect(
    authorId: Long?,
    onAuthorChanged: (Long) -> Unit,
    onAuthorColorAcquired: (Color) -> Unit,
    crowd: List<PersonaEntity>,
    modifier: Modifier = Modifier,
) {
    val selectedPersona = remember(authorId) {
        crowd.find { it.id == authorId }
    }

    var showBottomSheet by remember { mutableStateOf(false) }

    OutlinedButton(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        contentPadding = PaddingValues(4.dp),
        border = BorderStroke(width = 1.dp, color = Color.Gray),
        onClick = { showBottomSheet = true },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PersonaProfilePicture(
                    profilePic = selectedPersona?.profilePic,
                    modifier = Modifier
                        .size(64.dp)
                        .padding(6.dp)
                )
                Text(
                    text = selectedPersona?.name ?: stringResource(R.string.select_author)
                )
            }
            Icon(
                painter = painterResource(R.drawable.icon_expand),
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(32.dp)
            )
        }
    }

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    if (showBottomSheet) {
        AuthorSelectModal(
            sheetState,
            crowd,
            onDismissRequest = {
                scope.launch {
                    sheetState.hide()
                }.invokeOnCompletion {
                    showBottomSheet = false
                }
            },
            onPersonaSelected = { index ->
                onAuthorChanged(crowd[index].id)
                onAuthorColorAcquired(Color(crowd[index].colorTheme).darken())
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthorSelectModal(
    sheetState: SheetState,
    crowd: List<PersonaEntity>,
    onDismissRequest: () -> Unit,
    onPersonaSelected: (Int) -> Unit,
) {
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
        containerColor = Color.Black
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            crowd.forEachIndexed { index, entity ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp)
                        .clickable(onClick = {
                            onPersonaSelected(index)
                            onDismissRequest()
                        })
                ) {
                    PersonaProfilePicture(
                        profilePic = entity.profilePic,
                        modifier = Modifier.size(54.dp)
                    )
                    Text(
                        text = entity.name,
                    )
                }
            }
        }
    }
}