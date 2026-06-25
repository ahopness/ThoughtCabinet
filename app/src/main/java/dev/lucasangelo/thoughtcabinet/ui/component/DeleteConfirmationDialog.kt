package dev.lucasangelo.thoughtcabinet.ui.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.lucasangelo.thoughtcabinet.R

@Composable
fun DeleteConfirmationDialog(
    title: String = "Are you sure?",
    text: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        containerColor = Color.Black,
        icon = {
            Icon(
                painter = painterResource(R.drawable.icon_delete),
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
            )
        },
        title = { Text(title) },
        text = { Text(text) },
        onDismissRequest = onDismiss,
        dismissButton = {
            Button(onClick = onDismiss) { Text("Dismiss") }
        },
        confirmButton = {
            OutlinedButton(onClick = { onConfirm(); onDismiss() }) { Text("Confirm") }
        },
    )

}
