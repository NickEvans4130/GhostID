package com.ghostid.app.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ghostid.app.domain.model.toComposeColor
import com.ghostid.app.presentation.ui.components.AliasAvatarImage
import com.ghostid.app.presentation.ui.components.PasswordField
import com.ghostid.app.presentation.viewmodel.PasswordVaultViewModel
import com.ghostid.app.presentation.viewmodel.VaultEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordVaultScreen(
    viewModel: PasswordVaultViewModel = hiltViewModel(),
) {
    val entries by viewModel.filteredEntries.collectAsState()
    val query by viewModel.query.collectAsState()
    val revealedAccounts by viewModel.revealedAccounts.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Password Vault", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::setQuery,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search by alias, platform, or username…") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
            )

            if (entries.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (query.isEmpty()) "No passwords stored yet." else "No results for \"$query\"",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(entries, key = { "${it.alias.id}_${it.account.id}" }) { entry ->
                        VaultEntryCard(
                            entry = entry,
                            isRevealed = entry.account.id in revealedAccounts,
                            onToggleReveal = { viewModel.toggleReveal(entry.account.id) },
                            onCopy = { viewModel.copyPassword(entry.account) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VaultEntryCard(
    entry: VaultEntry,
    isRevealed: Boolean,
    onToggleReveal: () -> Unit,
    onCopy: () -> Unit,
) {
    val accentColor = entry.alias.accentColorInt.toComposeColor()
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AliasAvatarImage(
                    photoPath = entry.alias.photoPath,
                    name = entry.alias.name.full,
                    initials = entry.alias.name.initials,
                    accentColor = accentColor,
                    size = 36.dp,
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(entry.alias.name.full, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        entry.account.platform.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(entry.account.username, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            PasswordField(
                password = entry.account.password,
                isRevealed = isRevealed,
                onToggleReveal = onToggleReveal,
                onCopy = onCopy,
            )
        }
    }
}
