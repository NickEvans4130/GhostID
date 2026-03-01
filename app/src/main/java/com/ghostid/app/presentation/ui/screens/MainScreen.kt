package com.ghostid.app.presentation.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ghostid.app.domain.model.Alias
import com.ghostid.app.domain.model.HealthCheckWarning
import com.ghostid.app.domain.model.toComposeColor
import com.ghostid.app.presentation.ui.components.AliasAvatarImage
import com.ghostid.app.presentation.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onAliasClick: (String) -> Unit,
    viewModel: MainViewModel = hiltViewModel(),
) {
    val aliases by viewModel.aliases.collectAsState()
    val isCreating by viewModel.isCreating.collectAsState()
    val error by viewModel.error.collectAsState()
    val healthWarnings by viewModel.healthWarnings.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GhostID", fontWeight = FontWeight.Bold) },
                actions = {
                    if (healthWarnings.isNotEmpty()) {
                        IconButton(onClick = {}) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "${healthWarnings.size} health warning(s)",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    } else {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = "No issues detected",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 16.dp),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.createAlias() }) {
                if (isCreating) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Add, contentDescription = "Add Alias")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            AnimatedVisibility(
                visible = healthWarnings.isNotEmpty(),
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                HealthCheckBanner(
                    warnings = healthWarnings,
                    onDismiss = viewModel::dismissHealthWarnings,
                )
            }

            if (aliases.isEmpty() && !isCreating) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "No aliases yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Tap + to generate your first identity",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        )
                    }
                }
            } else {
                AliasTabRow(
                    aliases = aliases,
                    onAliasClick = onAliasClick,
                    isCreating = isCreating,
                )
            }
        }
    }
}

@Composable
private fun HealthCheckBanner(
    warnings: List<HealthCheckWarning>,
    onDismiss: () -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "${warnings.size} duplicate username${if (warnings.size > 1) "s" else ""} detected",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Show details",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.2f))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "These usernames appear across multiple aliases. Regenerate affected accounts to fix.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                    )
                    Spacer(Modifier.height(8.dp))
                    warnings.forEach { warning ->
                        HealthWarningRow(warning)
                        Spacer(Modifier.height(6.dp))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(
                                "Dismiss",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthWarningRow(warning: HealthCheckWarning) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.08f))
            .padding(8.dp),
    ) {
        Text(
            warning.duplicateValue,
            style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onErrorContainer,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(2.dp))
        warning.affectedAliases.forEach { alias ->
            Text(
                "  $alias",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.75f),
            )
        }
    }
}

@Composable
private fun AliasTabRow(
    aliases: List<Alias>,
    onAliasClick: (String) -> Unit,
    isCreating: Boolean,
) {
    val listState = rememberLazyListState()
    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(aliases, key = { it.id }) { alias ->
            AliasTabCard(alias = alias, onClick = { onAliasClick(alias.id) })
        }
        if (isCreating) {
            item {
                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun AliasTabCard(alias: Alias, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .size(width = 100.dp, height = 130.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = alias.accentColorInt.toComposeColor().copy(alpha = 0.15f),
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            alias.accentColorInt.toComposeColor().copy(alpha = 0.6f),
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            AliasAvatarImage(
                photoPath = alias.photoPath,
                name = alias.name.full,
                initials = alias.name.initials,
                accentColor = alias.accentColorInt.toComposeColor(),
                size = 56.dp,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = alias.name.firstName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = alias.accentColorInt.toComposeColor(),
                maxLines = 1,
            )
            Text(
                text = alias.name.lastName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 1,
            )
        }
    }
}
