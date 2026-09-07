package com.entrymyslot.app.screens.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.entrymyslot.app.EntryMySlotApp
import com.entrymyslot.app.data.mapper.toUi
import com.entrymyslot.app.data.model.UserProfile
import com.entrymyslotbe.app.core.ApiResult
import com.entrymyslotbe.app.network.model.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ProfileUiState(
    val isLoading: Boolean = false, val isLoggingOut: Boolean = false, val user: UserProfile? = null,
    val errorMessage: String? = null, val sessions: List<Session> = emptyList(), val successMessage: String? = null
)
class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as EntryMySlotApp).appContainer
    private val backend = container.backend
    private val _profileState = MutableStateFlow(ProfileUiState())
    val profileState: StateFlow<ProfileUiState> = _profileState.asStateFlow()
    init { loadProfile() }
    fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = _profileState.value.copy(isLoading = true, errorMessage = null)
            when (val result = backend.gateway.data { getMe() }) {
                is ApiResult.Success -> {
                    result.value?.let(backend.sessions::saveUser)
                    val city = getApplication<Application>().getSharedPreferences("entry_my_slot_preferences", 0).getString("selected_city", "").orEmpty()
                    val user = result.value?.toUi(city)
                    _profileState.value = _profileState.value.copy(isLoading = false, user = user, errorMessage = if (user == null) "Profile is unavailable." else null)
                }
                is ApiResult.Failure -> _profileState.value = _profileState.value.copy(isLoading = false, errorMessage = result.userMessage)
            }
        }
    }
    fun updateProfile(username: String, onSaved: () -> Unit = {}) {
        if (username.isBlank()) { _profileState.value = _profileState.value.copy(errorMessage = "Name is required."); return }
        viewModelScope.launch {
            when (val result = backend.gateway.data { updateProfile(UpdateProfileRequest(username.trim())) }) {
                is ApiResult.Success -> { loadProfile(); onSaved() }
                is ApiResult.Failure -> _profileState.value = _profileState.value.copy(errorMessage = result.userMessage)
            }
        }
    }
    fun changePassword(currentPassword: String, newPassword: String) {
        if (currentPassword.isBlank() || newPassword.length < 8) { _profileState.value = _profileState.value.copy(errorMessage = "Enter the current password and a new password of at least 8 characters."); return }
        viewModelScope.launch {
            when (val result = backend.gateway.data { changePassword(ChangePasswordRequest(currentPassword, newPassword)) }) {
                is ApiResult.Success -> _profileState.value = _profileState.value.copy(successMessage = result.message, errorMessage = null)
                is ApiResult.Failure -> _profileState.value = _profileState.value.copy(errorMessage = result.userMessage)
            }
        }
    }
    fun loadSessions() {
        viewModelScope.launch {
            when (val result = backend.gateway.data { getMySessions() }) {
                is ApiResult.Success -> _profileState.value = _profileState.value.copy(sessions = result.value.orEmpty())
                is ApiResult.Failure -> _profileState.value = _profileState.value.copy(errorMessage = result.userMessage)
            }
        }
    }
    fun revokeSession(id: String) {
        viewModelScope.launch {
            when (val result = backend.gateway.data { revokeMySession(RevokeSessionRequest(id)) }) {
                is ApiResult.Success -> loadSessions()
                is ApiResult.Failure -> _profileState.value = _profileState.value.copy(errorMessage = result.userMessage)
            }
        }
    }
    fun logout(onFinished: () -> Unit) {
        if (_profileState.value.isLoggingOut) return
        viewModelScope.launch {
            _profileState.value = _profileState.value.copy(isLoggingOut = true)
            val result = backend.auth.logout()
            container.pendingCheckoutStore.clear()
            _profileState.value = ProfileUiState(errorMessage = (result as? ApiResult.Failure)?.userMessage)
            onFinished()
        }
    }
}
