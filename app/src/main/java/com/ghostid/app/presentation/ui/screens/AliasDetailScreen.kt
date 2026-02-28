package com.ghostid.app.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.ContactMail
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ghostid.app.domain.model.Account
import com.ghostid.app.domain.model.Alias
import com.ghostid.app.domain.model.Platform
import com.ghostid.app.domain.model.PlatformCategory
import com.ghostid.app.domain.model.toComposeColor
import com.ghostid.app.presentation.ui.components.AliasAvatarImage
import com.ghostid.app.presentation.ui.components.ExpandableCard
import com.ghostid.app.presentation.ui.components.PasswordField
import com.ghostid.app.presentation.ui.components.QrCodeDialog
import com.ghostid.app.presentation.viewmodel.AliasDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AliasDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: AliasDetailViewModel = hiltViewModel(),
) {
    val alias by viewModel.alias.collectAsState()
    val revealedPasswords by viewModel.revealedPasswords.collectAsState()
    val exportedPath by viewModel.exportedPath.collectAsState()
    var showQrDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(alias?.name?.full ?: "Loading…") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showQrDialog = true }) {
                        Icon(Icons.Default.QrCode, contentDescription = "Generate QR Code")
                    }
                    IconButton(onClick = { viewModel.exportAlias() }) {
                        Icon(Icons.Default.Download, contentDescription = "Export Alias")
                    }
                },
            )
        }
    ) { padding ->
        alias?.let { al ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Header
                AliasHeader(al)

                // Identity Card
                ExpandableCard(
                    title = "Identity Card",
                    icon = Icons.Default.Badge,
                    initiallyExpanded = true,
                ) {
                    IdentitySection(al)
                }

                // Contact Details
                ExpandableCard(title = "Contact Details", icon = Icons.Default.ContactMail) {
                    ContactSection(al)
                }

                // Platform Accounts
                PlatformCategory.entries.forEach { category ->
                    val accounts = al.accounts.filter { it.platform.category == category }
                    if (accounts.isNotEmpty()) {
                        ExpandableCard(
                            title = category.name.lowercase().replaceFirstChar { it.uppercase() },
                            icon = Icons.Default.Lock,
                        ) {
                            accounts.forEach { account ->
                                AccountRow(
                                    account = account,
                                    isRevealed = account.id in revealedPasswords,
                                    onToggleReveal = { viewModel.togglePasswordVisibility(account.id) },
                                    onCopyPassword = {
                                        viewModel.copyToClipboard("GhostID password", account.password)
                                    },
                                    onCopyUsername = {
                                        viewModel.copyToClipboard("GhostID username", account.username)
                                    },
                                )
                                Divider(modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }

                // Notes
                ExpandableCard(title = "Notes", icon = Icons.Default.Note) {
                    NotesSection(
                        notes = al.notes,
                        onNotesChange = viewModel::updateNotes,
                    )
                }

                // Security info
                ExpandableCard(title = "Security", icon = Icons.Default.Security) {
                    SecuritySection(al)
                }

                // Actions
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Delete Alias")
                }
                Spacer(Modifier.height(32.dp))
            }

            if (showQrDialog) {
                QrCodeDialog(alias = al, onDismiss = { showQrDialog = false })
            }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Alias") },
                    text = { Text("Permanently delete ${al.name.full}? This cannot be undone.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDeleteDialog = false
                                viewModel.deleteAlias(onDeleted = onNavigateBack)
                            }
                        ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
                    },
                )
            }

            exportedPath?.let { path ->
                AlertDialog(
                    onDismissRequest = { viewModel.dismissExportPath() },
                    title = { Text("Export Successful") },
                    text = { Text("Saved to:\n$path") },
                    confirmButton = {
                        TextButton(onClick = { viewModel.dismissExportPath() }) { Text("OK") }
                    },
                )
            }
        }
    }
}

@Composable
private fun AliasHeader(alias: Alias) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AliasAvatarImage(
            photoPath = alias.photoPath,
            name = alias.name.full,
            initials = alias.name.initials,
            accentColor = alias.accentColorInt.toComposeColor(),
            size = 80.dp,
        )
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                alias.name.full,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                alias.occupation,
                style = MaterialTheme.typography.bodyMedium,
                color = alias.accentColorInt.toComposeColor(),
            )
            Text(
                alias.nationality,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
    }
}

@Composable
private fun IdentitySection(alias: Alias) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        DetailRow("Full Name", alias.name.full)
        DetailRow("Date of Birth", alias.dateOfBirth)
        DetailRow("Star Sign", alias.starSign)
        DetailRow("Blood Type", alias.bloodType)
        DetailRow("Nationality", alias.nationality)
        DetailRow("Occupation", alias.occupation)
        Spacer(Modifier.height(4.dp))
        Text(
            "Bio",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
        Text(alias.bio, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ContactSection(alias: Alias) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        DetailRow("Phone", alias.phoneNumber)
        DetailRow("Street", alias.address.street)
        DetailRow("City", alias.address.city)
        DetailRow("Postcode", alias.address.postcode)
        DetailRow("Country", alias.address.country)
    }
}

@Composable
private fun AccountRow(
    account: Account,
    isRevealed: Boolean,
    onToggleReveal: () -> Unit,
    onCopyPassword: () -> Unit,
    onCopyUsername: () -> Unit,
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                account.platform.displayName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
            IconButton(onClick = onCopyUsername, modifier = Modifier.size(28.dp)) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = "Copy username",
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Text(
            account.username,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )
        PasswordField(
            password = account.password,
            isRevealed = isRevealed,
            onToggleReveal = onToggleReveal,
            onCopy = onCopyPassword,
        )
    }
}

@Composable
private fun NotesSection(notes: String, onNotesChange: (String) -> Unit) {
    var text by rememberSaveable(notes) { mutableStateOf(notes) }
    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Notes (encrypted)") },
        placeholder = { Text("e.g. Used this alias for forum X") },
        minLines = 3,
    )
    Spacer(Modifier.height(4.dp))
    TextButton(onClick = { onNotesChange(text) }) { Text("Save Notes") }
}

@Composable
private fun SecuritySection(alias: Alias) {
    val created = remember(alias.createdAt) {
        java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
            .format(java.util.Date(alias.createdAt))
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        DetailRow("Created", created)
        DetailRow("Password count", "${alias.accounts.size} unique passwords")
        DetailRow("Storage", "Encrypted with AES-256-GCM")
        if (alias.tags.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text("Tags", style = MaterialTheme.typography.labelSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                alias.tags.forEach { tag ->
                    AssistChip(onClick = {}, label = { Text(tag) })
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.weight(0.4f),
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(0.6f),
        )
    }
}
