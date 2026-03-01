package com.ghostid.app.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghostid.app.data.network.MessageSummary
import com.ghostid.app.data.network.TempEmailRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InboxViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tempEmailRepository: TempEmailRepository,
) : ViewModel() {

    private val aliasId: String = checkNotNull(savedStateHandle["aliasId"])

    private val _inboxAddress = MutableStateFlow<String?>(null)
    val inboxAddress: StateFlow<String?> = _inboxAddress.asStateFlow()

    private val _messages = MutableStateFlow<List<MessageSummary>>(emptyList())
    val messages: StateFlow<List<MessageSummary>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _hasInbox = MutableStateFlow(false)
    val hasInbox: StateFlow<Boolean> = _hasInbox.asStateFlow()

    init {
        viewModelScope.launch {
            val address = tempEmailRepository.getInboxAddress(aliasId)
            _inboxAddress.value = address
            _hasInbox.value = address != null
            if (address != null) refreshMessages()
        }
    }

    fun refreshMessages() {
        viewModelScope.launch {
            _isLoading.value = true
            _messages.value = tempEmailRepository.fetchMessages(aliasId)
            _isLoading.value = false
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            tempEmailRepository.deleteMessage(aliasId, messageId)
            _messages.value = _messages.value.filter { it.id != messageId }
        }
    }

    fun deleteInbox(onDeleted: () -> Unit) {
        viewModelScope.launch {
            tempEmailRepository.deleteInbox(aliasId)
            _inboxAddress.value = null
            _hasInbox.value = false
            _messages.value = emptyList()
            onDeleted()
        }
    }
}
