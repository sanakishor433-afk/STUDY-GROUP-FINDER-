package com.studygroup.finder.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studygroup.finder.data.model.ChatMessage
import com.studygroup.finder.data.repository.AuthRepository
import com.studygroup.finder.data.repository.ChatRepository
import com.studygroup.finder.data.repository.StudyGroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the chat screen.
 *
 * @property messages     ordered list of messages (oldest → newest).
 * @property currentUserId UID of the signed-in user (used for bubble alignment).
 * @property groupName    display name of the group (shown in the top bar).
 * @property isLoading    true while the initial message snapshot is loading.
 * @property errorMessage non-null when an error needs surfacing.
 */
data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val currentUserId: String = "",
    val groupName: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

/**
 * ViewModel for the real-time group chat screen.
 *
 * Streams messages from Firestore via [ChatRepository.getMessagesFlow] and
 * exposes a send action that writes a new [ChatMessage] with the current
 * user's identity.
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    private val studyGroupRepository: StudyGroupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    /** Tracks the group being observed to avoid duplicate collectors. */
    private var observedGroupId: String? = null

    /**
     * Start observing real-time messages for [groupId].
     *
     * Safe to call multiple times — subsequent calls with the same ID are
     * no-ops.
     */
    fun loadMessages(groupId: String) {
        if (groupId == observedGroupId) return
        observedGroupId = groupId

        // Set current user
        val uid = authRepository.getCurrentUser()?.uid.orEmpty()
        _uiState.update { it.copy(currentUserId = uid, isLoading = true) }

        // Load group name for the top bar
        viewModelScope.launch {
            studyGroupRepository.getGroupById(groupId)
                .onSuccess { group ->
                    _uiState.update { it.copy(groupName = group.name) }
                }
        }

        // Stream messages
        viewModelScope.launch {
            chatRepository.getMessagesFlow(groupId)
                .catch { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.localizedMessage
                                ?: "Failed to load messages"
                        )
                    }
                }
                .collect { messages ->
                    _uiState.update {
                        it.copy(messages = messages, isLoading = false)
                    }
                }
        }
    }

    /**
     * Send a text message to the group.
     *
     * Resolves the sender name from the current Firebase user's display
     * name (or falls back to "Unknown").
     */
    fun sendMessage(groupId: String, content: String) {
        if (content.isBlank()) return

        val firebaseUser = authRepository.getCurrentUser() ?: return
        val message = ChatMessage(
            groupId = groupId,
            senderId = firebaseUser.uid,
            senderName = firebaseUser.displayName ?: "Unknown",
            content = content.trim(),
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch {
            chatRepository.sendMessage(message)
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            errorMessage = throwable.localizedMessage
                                ?: "Failed to send message"
                        )
                    }
                }
        }
    }

    /** Clear any displayed error after the UI has shown it. */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
