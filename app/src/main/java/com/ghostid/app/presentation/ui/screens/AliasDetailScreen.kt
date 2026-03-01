package com.ghostid.app.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContactMail
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ghostid.app.data.network.MessageSummary
import com.ghostid.app.domain.model.Account
import com.ghostid.app.domain.model.AccountStatus
import com.ghostid.app.domain.model.Alias
import com.ghostid.app.domain.model.Platform
import com.ghostid.app.domain.model.PlatformCategory
import com.ghostid.app.domain.model.toComposeColor
import com.ghostid.app.presentation.ui.components.AliasAvatarImage
import com.ghostid.app.presentation.ui.components.ExpandableCard
import com.ghostid.app.presentation.ui.components.PasswordField
import com.ghostid.app.presentation.ui.components.QrCodeDialog
import com.ghostid.app.presentation.viewmodel.AliasDetailViewModel
import com.ghostid.app.presentation.viewmodel.InboxViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AliasDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMessage: (aliasId: String, messageId: String) -> Unit,
    viewModel: AliasDetailViewModel = hiltViewModel(),
    inboxViewModel: InboxViewModel = hiltViewModel(),
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
                AliasHeader(al)

                ExpandableCard(title = "Identity Card", icon = Icons.Default.Badge, initiallyExpanded = true) {
                    IdentitySection(al)
                }

                ExpandableCard(title = "Contact Details", icon = Icons.Default.ContactMail) {
                    ContactSection(al)
                }

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

                // Temp Inbox
                InboxSection(
                    aliasId = al.id,
                    viewModel = inboxViewModel,
                    onMessageClick = { messageId ->
                        onNavigateToMessage(al.id, messageId)
                    },
                )

                // Setup Assistant
                SetupAssistantSection(
                    alias = al,
                    viewModel = viewModel,
                )

                ExpandableCard(title = "Notes", icon = Icons.Default.Note) {
                    NotesSection(notes = al.notes, onNotesChange = viewModel::updateNotes)
                }

                ExpandableCard(title = "Security", icon = Icons.Default.Security) {
                    SecuritySection(al)
                }

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

// --- Inbox Section ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InboxSection(
    aliasId: String,
    viewModel: InboxViewModel,
    onMessageClick: (String) -> Unit,
) {
    val inboxAddress by viewModel.inboxAddress.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val hasInbox by viewModel.hasInbox.collectAsState()
    var showDeleteInboxDialog by remember { mutableStateOf(false) }

    LaunchedEffect(hasInbox) {
        if (hasInbox) {
            while (true) {
                viewModel.refreshMessages()
                delay(15_000)
            }
        }
    }

    val unreadCount = messages.count { !it.seen }
    val cardTitle = if (unreadCount > 0) "Temp Inbox ($unreadCount new)" else "Temp Inbox"

    ExpandableCard(title = cardTitle, icon = Icons.Default.Email) {
        if (!hasInbox) {
            Text(
                "No inbox created for this alias.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
            return@ExpandableCard
        }

        inboxAddress?.let { address ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = { /* copyToClipboard handled by parent */ },
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Copy address", modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        if (isLoading && messages.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        } else if (messages.isEmpty()) {
            Text(
                "No messages yet.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
        } else {
            messages.forEach { msg ->
                MessageRow(
                    message = msg,
                    onClick = { onMessageClick(msg.id) },
                    onDelete = { viewModel.deleteMessage(msg.id) },
                )
                Divider(modifier = Modifier.padding(vertical = 2.dp))
            }
        }

        Spacer(Modifier.height(8.dp))
        TextButton(
            onClick = { showDeleteInboxDialog = true },
            colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.error,
            ),
        ) {
            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Delete Inbox")
        }
    }

    if (showDeleteInboxDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteInboxDialog = false },
            title = { Text("Delete Inbox") },
            text = { Text("This will permanently delete the temp inbox and all its messages. Cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteInboxDialog = false
                    viewModel.deleteInbox {}
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteInboxDialog = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun MessageRow(
    message: MessageSummary,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val dateFmt = remember { SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!message.seen) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(MaterialTheme.colorScheme.primary, shape = androidx.compose.foundation.shape.CircleShape),
            )
            Spacer(Modifier.width(8.dp))
        } else {
            Spacer(Modifier.width(16.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                message.from.address,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (!message.seen) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
            )
            Text(
                message.subject,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (!message.seen) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
            )
            Text(
                message.intro,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                runCatching { dateFmt.format(java.util.Date(message.createdAt.take(10).replace("-", "").let { 0L })) }
                    .getOrDefault(message.createdAt.take(16).replace("T", " ")),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
            IconButton(onClick = onClick, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Launch, contentDescription = "Open", modifier = Modifier.size(16.dp))
            }
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

// --- Setup Assistant Section ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetupAssistantSection(
    alias: Alias,
    viewModel: AliasDetailViewModel,
) {
    val accounts = alias.accounts
    val createdCount = accounts.count { it.status == AccountStatus.CREATED }
    val totalCount = accounts.size
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var selectedAccount by remember { mutableStateOf<Account?>(null) }

    ExpandableCard(title = "Setup Assistant", icon = Icons.Default.Speed) {
        // Progress bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                "$createdCount / $totalCount accounts created",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = if (totalCount > 0) createdCount.toFloat() / totalCount else 0f,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))

        accounts.forEach { account ->
            SetupAccountRow(
                account = account,
                onClick = { selectedAccount = account },
            )
            Divider(modifier = Modifier.padding(vertical = 2.dp))
        }
    }

    // Bottom sheet for account setup
    selectedAccount?.let { account ->
        ModalBottomSheet(
            onDismissRequest = { selectedAccount = null },
            sheetState = sheetState,
        ) {
            SetupAccountSheet(
                account = account,
                inboxAddress = null, // shown from InboxViewModel but not easily passed here; user can see it above
                onLaunchSignup = {
                    viewModel.launchSignup(account)
                },
                onMarkCreated = {
                    viewModel.markAccountCreated(account.id)
                    scope.launch { sheetState.hide() }.invokeOnCompletion { selectedAccount = null }
                },
                onSkip = {
                    viewModel.markAccountSkipped(account.id)
                    scope.launch { sheetState.hide() }.invokeOnCompletion { selectedAccount = null }
                },
                onDismiss = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion { selectedAccount = null }
                },
            )
        }
    }
}

@Composable
private fun SetupAccountRow(account: Account, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(account.platform.displayName, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Text(account.username, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, maxLines = 1)
        }
        when (account.status) {
            AccountStatus.CREATED -> Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Created",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(20.dp),
            )
            AccountStatus.REQUIRES_PHONE -> Icon(
                Icons.Default.Phone,
                contentDescription = "Requires phone",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(20.dp),
            )
            AccountStatus.SKIPPED -> Text(
                "Skipped",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            )
            AccountStatus.PENDING -> TextButton(onClick = onClick) { Text("Set up") }
        }
        if (account.status != AccountStatus.PENDING) {
            TextButton(onClick = onClick) {
                Text("Edit", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun SetupAccountSheet(
    account: Account,
    inboxAddress: String?,
    onLaunchSignup: () -> Unit,
    onMarkCreated: () -> Unit,
    onSkip: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(account.platform.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        if (account.platform.requiresPhone) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.small,
                    )
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.Phone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "This platform requires a phone number for signup.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }

        DetailRow("Username", account.username)
        Text("Hint: ${account.platform.usernameHint}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))

        PasswordField(
            password = account.password,
            isRevealed = false,
            onToggleReveal = {},
            onCopy = onLaunchSignup, // copying happens inside launchSignup
        )

        if (inboxAddress != null) {
            DetailRow("Temp email", inboxAddress)
        }

        Spacer(Modifier.height(4.dp))

        Button(onClick = onLaunchSignup, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("Open signup page")
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = onMarkCreated,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
            ) {
                Text("Mark as created")
            }
            TextButton(onClick = onSkip, modifier = Modifier.weight(1f)) {
                Text("Skip")
            }
        }
        TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text("Close")
        }
        Spacer(Modifier.height(16.dp))
    }
}

// --- Existing section composables ---

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
