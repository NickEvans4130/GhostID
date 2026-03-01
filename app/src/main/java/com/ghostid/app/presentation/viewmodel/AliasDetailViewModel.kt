package com.ghostid.app.presentation.viewmodel

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghostid.app.data.repository.AliasRepository
import com.ghostid.app.domain.model.Account
import com.ghostid.app.domain.model.AccountStatus
import com.ghostid.app.domain.model.Alias
import com.ghostid.app.domain.usecase.DeleteAliasUseCase
import com.ghostid.app.domain.usecase.ExportAliasUseCase
import com.ghostid.app.utils.ClipboardClearService
import com.ghostid.app.utils.DeepLinkLauncher
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AliasDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: AliasRepository,
    private val deleteAliasUseCase: DeleteAliasUseCase,
    private val exportAliasUseCase: ExportAliasUseCase,
    private val deepLinkLauncher: DeepLinkLauncher,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val aliasId: String = checkNotNull(savedStateHandle["aliasId"])

    private val _alias = MutableStateFlow<Alias?>(null)
    val alias: StateFlow<Alias?> = _alias.asStateFlow()

    private val _exportedPath = MutableStateFlow<String?>(null)
    val exportedPath: StateFlow<String?> = _exportedPath.asStateFlow()

    private val _revealedPasswords = MutableStateFlow<Set<String>>(emptySet())
    val revealedPasswords: StateFlow<Set<String>> = _revealedPasswords.asStateFlow()

    private val _clipboardTimeoutMs = MutableStateFlow(30_000L)

    init {
        loadAlias()
    }

    private fun loadAlias() {
        viewModelScope.launch {
            _alias.value = repository.getAliasById(aliasId)
        }
    }

    fun togglePasswordVisibility(accountId: String) {
        val current = _revealedPasswords.value
        _revealedPasswords.value = if (accountId in current) {
            current - accountId
        } else {
            current + accountId
        }
    }

    fun copyToClipboard(label: String, value: String) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText(label, value))
        ClipboardClearService.startWithTimeout(context, _clipboardTimeoutMs.value)
    }

    fun updateNotes(notes: String) {
        viewModelScope.launch {
            val current = _alias.value ?: return@launch
            val updated = current.copy(notes = notes)
            repository.updateAlias(updated)
            _alias.value = updated
        }
    }

    fun updateTags(tags: List<String>) {
        viewModelScope.launch {
            val current = _alias.value ?: return@launch
            val updated = current.copy(tags = tags)
            repository.updateAlias(updated)
            _alias.value = updated
        }
    }

    fun exportAlias() {
        viewModelScope.launch {
            _exportedPath.value = exportAliasUseCase.invoke(aliasId)
        }
    }

    fun deleteAlias(onDeleted: () -> Unit) {
        viewModelScope.launch {
            val alias = _alias.value ?: return@launch
            deleteAliasUseCase(alias.id, alias.photoPath)
            onDeleted()
        }
    }

    fun setClipboardTimeout(ms: Long) {
        _clipboardTimeoutMs.value = ms
    }

    fun dismissExportPath() { _exportedPath.value = null }

    // --- Setup assistant ---

    fun markAccountCreated(accountId: String) {
        viewModelScope.launch {
            repository.updateAccountStatus(accountId, AccountStatus.CREATED, System.currentTimeMillis())
            loadAlias()
        }
    }

    fun markAccountSkipped(accountId: String) {
        viewModelScope.launch {
            repository.updateAccountStatus(accountId, AccountStatus.SKIPPED, null)
            loadAlias()
        }
    }

    fun launchSignup(account: Account) {
        deepLinkLauncher.launchSignup(account)
    }
}
