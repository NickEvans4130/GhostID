package com.ghostid.app.presentation.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.ghostid.app.domain.model.Alias
import com.ghostid.app.utils.QrCodeGenerator

@Composable
fun QrCodeDialog(
    alias: Alias,
    onDismiss: () -> Unit,
) {
    val bitmap = remember(alias.id) { QrCodeGenerator.generateForAlias(alias) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("QR Code — ${alias.name.full}") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "QR code for ${alias.name.full}",
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Contains fake vCard contact details only",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
    )
}
