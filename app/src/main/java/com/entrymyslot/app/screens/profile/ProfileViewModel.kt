package com.entrymyslot.app.screens.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.entrymyslot.app.EntryMySlotApp
import com.entrymyslot.app.data.mapper.toUi
import com.entrymyslot.app.data.model.UserProfile
import com.entrymyslotbe.app.auth.AuthScope
import com.entrymyslotbe.app.core.ApiResult
import com.entrymyslotbe.app.network.model.UpdateProfileRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isLoggingOut: Boolean = false,
    val user: UserProfile? = null,
    val errorMessage: String? = null
)
class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as EntryMySlotApp).appContainer
    private val backend = container.backend
    private val _profileState = MutableStateFlow(ProfileUiState())
    val profileState: StateFlow<ProfileUiState> = _profileState.asStateFlow()
    init { loadProfile() }
    fun loadProfile() {
        if (_profileState.value.isLoading || _profileState.value.isLoggingOut) return
        val expectedSession = backend.sessions.snapshot(AuthScope.USER)
        _profileState.value = _profileState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val result = backend.gateway.data { getMe() }
            val currentSession = backend.sessions.snapshot(AuthScope.USER)
            if (currentSession.generation != expectedSession.generation || currentSession.accessToken.isNullOrBlank()) return@launch
            when (result) {
                is ApiResult.Success -> {
                    if (result.value != null && !backend.sessions.saveUserIfCurrent(expectedSession, result.value)) return@launch
                    val city = getApplication<Application>().getSharedPreferences("entry_my_slot_preferences", 0).getString("selected_city", "").orEmpty()
                    val user = result.value?.toUi(city)
                    _profileState.value = _profileState.value.copy(isLoading = false, user = user, errorMessage = if (user == null) "Profile is unavailable." else null)
                }
                is ApiResult.Failure -> _profileState.value = _profileState.value.copy(isLoading = false, errorMessage = result.userMessage)
            }
        }
    }
    fun updateProfile(username: String, onSaved: () -> Unit = {}) {
        if (_profileState.value.isSaving || _profileState.value.isLoggingOut) return
        if (username.isBlank()) { _profileState.value = _profileState.value.copy(errorMessage = "Name is required."); return }
        val expectedSession = backend.sessions.snapshot(AuthScope.USER)
        _profileState.value = _profileState.value.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            val result = backend.gateway.data { updateProfile(UpdateProfileRequest(username.trim())) }
            val currentSession = backend.sessions.snapshot(AuthScope.USER)
            if (currentSession.generation != expectedSession.generation || currentSession.accessToken.isNullOrBlank()) return@launch
            when (result) {
                is ApiResult.Success -> {
                    if (result.value != null && !backend.sessions.saveUserIfCurrent(expectedSession, result.value)) return@launch
                    val city = getApplication<Application>().getSharedPreferences("entry_my_slot_preferences", 0).getString("selected_city", "").orEmpty()
                    val updatedUser = result.value?.toUi(city)
                    _profileState.value = _profileState.value.copy(isSaving = false, user = updatedUser ?: _profileState.value.user)
                    if (updatedUser == null) loadProfile()
                    onSaved()
                }
                is ApiResult.Failure -> _profileState.value = _profileState.value.copy(isSaving = false, errorMessage = result.userMessage)
            }
        }
    }
    fun clearError() { _profileState.value = _profileState.value.copy(errorMessage = null) }
    fun logout(onFinished: () -> Unit) {
        if (_profileState.value.isLoggingOut) return
        _profileState.value = _profileState.value.copy(isLoggingOut = true)
        viewModelScope.launch {
            val result = backend.auth.logout()
            container.pendingCheckoutStore.clear()
            _profileState.value = ProfileUiState(errorMessage = (result as? ApiResult.Failure)?.userMessage)
            onFinished()
        }
    }
}
