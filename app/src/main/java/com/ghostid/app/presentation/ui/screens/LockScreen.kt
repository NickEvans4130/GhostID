package com.ghostid.app.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LockScreen(
    onUnlockClick: () -> Unit,
    errorMessage: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Fingerprint,
            contentDescription = null,
            tint = Color(0xFF6C63FF),
            modifier = Modifier.size(80.dp),
        )

        Spacer(Modifier.height(24.dp))

        Text(
            "GhostID",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Locked",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.5f),
        )

        Spacer(Modifier.height(48.dp))

        Button(
            onClick = onUnlockClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF)),
        ) {
            Icon(
                Icons.Default.Fingerprint,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.height(8.dp))
            Text("Unlock")
        }

        if (errorMessage != null) {
            Spacer(Modifier.height(24.dp))
            Text(
                errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFE94560),
                textAlign = TextAlign.Center,
            )
        }
    }
}
