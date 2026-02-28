package com.ghostid.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghostid.app.data.repository.AliasRepository
import com.ghostid.app.domain.model.Alias
import com.ghostid.app.domain.usecase.CreateAliasUseCase
import com.ghostid.app.domain.usecase.DeleteAliasUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: AliasRepository,
    private val createAliasUseCase: CreateAliasUseCase,
    private val deleteAliasUseCase: DeleteAliasUseCase,
) : ViewModel() {

    private val _aliases = MutableStateFlow<List<Alias>>(emptyList())
    val aliases: StateFlow<List<Alias>> = _aliases.asStateFlow()

    private val _isCreating = MutableStateFlow(false)
    val isCreating: StateFlow<Boolean> = _isCreating.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex.asStateFlow()

    private val _duplicateWarning = MutableStateFlow(false)
    val duplicateWarning: StateFlow<Boolean> = _duplicateWarning.asStateFlow()

    init {
        repository.observeAllAliases()
            .onEach { _aliases.value = it }
            .launchIn(viewModelScope)
    }

    fun selectTab(index: Int) {
        _selectedTabIndex.value = index
    }

    fun createAlias() {
        if (_isCreating.value) return
        viewModelScope.launch {
            _isCreating.value = true
            runCatching { createAliasUseCase() }
                .onSuccess { newAlias ->
                    val newIndex = _aliases.value.indexOfFirst { it.id == newAlias.id }
                    if (newIndex >= 0) _selectedTabIndex.value = newIndex
                }
                .onFailure { _error.value = it.message }
            _isCreating.value = false
        }
    }

    fun deleteAlias(alias: Alias) {
        viewModelScope.launch {
            deleteAliasUseCase(alias.id, alias.photoPath)
        }
    }

    fun runHealthCheck() {
        viewModelScope.launch {
            val duplicates = repository.findDuplicateUsernames()
            _duplicateWarning.value = duplicates.isNotEmpty()
        }
    }

    fun dismissError() { _error.value = null }
    fun dismissDuplicateWarning() { _duplicateWarning.value = false }
}
