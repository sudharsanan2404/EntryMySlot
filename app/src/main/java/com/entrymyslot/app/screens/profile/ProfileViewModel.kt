package com.entrymyslot.app.screens.profile

import androidx.lifecycle.ViewModel
import com.entrymyslot.app.data.FakeData
import com.entrymyslot.app.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isLoggingOut: Boolean = false,
    val user: UserProfile? = null,
    val errorMessage: String? = null
)

class ProfileViewModel : ViewModel() {
    private val _profileState = MutableStateFlow(ProfileUiState(user = previewUser()))
    val profileState: StateFlow<ProfileUiState> = _profileState.asStateFlow()

    fun loadProfile() { _profileState.value = ProfileUiState(user = previewUser()) }

    fun logout(onFinished: () -> Unit) {
        _profileState.value = _profileState.value.copy(isLoggingOut = true)
        onFinished()
        _profileState.value = _profileState.value.copy(isLoggingOut = false)
    }

    private fun previewUser() = FakeData.currentUser
}
