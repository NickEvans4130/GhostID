package com.ghostid.app.presentation.viewmodel

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghostid.app.data.db.AliasDao
import com.ghostid.app.data.repository.AliasRepository
import com.ghostid.app.domain.model.Account
import com.ghostid.app.domain.model.Alias
import com.ghostid.app.utils.ClipboardClearService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VaultEntry(
    val alias: Alias,
    val account: Account,
)

@HiltViewModel
class PasswordVaultViewModel @Inject constructor(
    private val repository: AliasRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _allEntries = MutableStateFlow<List<VaultEntry>>(emptyList())

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _filteredEntries = MutableStateFlow<List<VaultEntry>>(emptyList())
    val filteredEntries: StateFlow<List<VaultEntry>> = _filteredEntries.asStateFlow()

    private val _revealedAccounts = MutableStateFlow<Set<String>>(emptySet())
    val revealedAccounts: StateFlow<Set<String>> = _revealedAccounts.asStateFlow()

    init {
        repository.observeAllAliases()
            .onEach { aliases ->
                val entries = aliases.flatMap { alias ->
                    alias.accounts.map { VaultEntry(alias, it) }
                }
                _allEntries.value = entries
                applyFilter()
            }
            .launchIn(viewModelScope)
    }

    fun setQuery(q: String) {
        _query.value = q
        applyFilter()
    }

    private fun applyFilter() {
        val q = _query.value.trim().lowercase()
        _filteredEntries.value = if (q.isEmpty()) {
            _allEntries.value
        } else {
            _allEntries.value.filter { entry ->
                entry.alias.name.full.lowercase().contains(q) ||
                    entry.account.platform.displayName.lowercase().contains(q) ||
                    entry.account.username.lowercase().contains(q)
            }
        }
    }

    fun toggleReveal(accountId: String) {
        val current = _revealedAccounts.value
        _revealedAccounts.value = if (accountId in current) current - accountId else current + accountId
    }

    fun copyPassword(account: Account, timeoutMs: Long = 30_000L) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("GhostID password", account.password))
        ClipboardClearService.startWithTimeout(context, timeoutMs)
    }
}
