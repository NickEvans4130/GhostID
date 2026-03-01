package com.ghostid.app.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghostid.app.data.network.MessageDetail
import com.ghostid.app.data.network.TempEmailRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessageDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tempEmailRepository: TempEmailRepository,
) : ViewModel() {

    private val aliasId: String = checkNotNull(savedStateHandle["aliasId"])
    private val messageId: String = checkNotNull(savedStateHandle["messageId"])

    private val _message = MutableStateFlow<MessageDetail?>(null)
    val message: StateFlow<MessageDetail?> = _message.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            _message.value = tempEmailRepository.fetchMessageDetail(aliasId, messageId)
            _isLoading.value = false
        }
    }
}
