package com.ghostid.app.presentation.viewmodel

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghostid.app.data.repository.AliasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

private val Context.settingsDataStore by preferencesDataStore("ghostid_settings")

enum class AppTheme { LIGHT, DARK, AMOLED }

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: AliasRepository,
) : ViewModel() {

    companion object {
        val KEY_APP_LOCK = booleanPreferencesKey("app_lock")
        val KEY_CLIPBOARD_TIMEOUT = longPreferencesKey("clipboard_timeout_ms")
        val KEY_THEME = intPreferencesKey("theme_ordinal")
    }

    private val _appLock = MutableStateFlow(false)
    val appLock: StateFlow<Boolean> = _appLock.asStateFlow()

    private val _clipboardTimeoutMs = MutableStateFlow(30_000L)
    val clipboardTimeoutMs: StateFlow<Long> = _clipboardTimeoutMs.asStateFlow()

    private val _theme = MutableStateFlow(AppTheme.DARK)
    val theme: StateFlow<AppTheme> = _theme.asStateFlow()

    private val _wipeInProgress = MutableStateFlow(false)
    val wipeInProgress: StateFlow<Boolean> = _wipeInProgress.asStateFlow()

    init {
        context.settingsDataStore.data
            .map { prefs ->
                Triple(
                    prefs[KEY_APP_LOCK] ?: false,
                    prefs[KEY_CLIPBOARD_TIMEOUT] ?: 30_000L,
                    AppTheme.entries.getOrElse(prefs[KEY_THEME] ?: 1) { AppTheme.DARK },
                )
            }
            .onEach { (lock, timeout, theme) ->
                _appLock.value = lock
                _clipboardTimeoutMs.value = timeout
                _theme.value = theme
            }
            .launchIn(viewModelScope)
    }

    fun setAppLock(enabled: Boolean) {
        viewModelScope.launch {
            context.settingsDataStore.edit { it[KEY_APP_LOCK] = enabled }
        }
    }

    fun setClipboardTimeout(ms: Long) {
        viewModelScope.launch {
            context.settingsDataStore.edit { it[KEY_CLIPBOARD_TIMEOUT] = ms }
        }
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            context.settingsDataStore.edit { it[KEY_THEME] = theme.ordinal }
        }
    }

    fun wipeAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            _wipeInProgress.value = true
            val aliases = repository.getAllAliases()
            aliases.forEach { alias -> repository.deleteAlias(alias.id) }
            context.settingsDataStore.edit { it.clear() }
            _wipeInProgress.value = false
            onComplete()
        }
    }
}
