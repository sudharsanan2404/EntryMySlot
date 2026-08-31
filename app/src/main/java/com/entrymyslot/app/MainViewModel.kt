package com.entrymyslot.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.entrymyslot.app.core.network.NetworkMonitor
import com.entrymyslot.app.data.auth.ApiException
import com.entrymyslot.app.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AppAuthState {
    data object Loading : AppAuthState
    data object Authenticated : AppAuthState
    data object AuthenticatedOffline : AppAuthState
    data object Unauthenticated : AppAuthState
}

class MainViewModel(
    private val authRepository: AuthRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {
    private val _authState = MutableStateFlow<AppAuthState>(AppAuthState.Loading)
    val authState: StateFlow<AppAuthState> = _authState.asStateFlow()

    init {
        observeSessionAndConnectivity()
    }

    private fun observeSessionAndConnectivity() {
        viewModelScope.launch {
            var lastOnline = networkMonitor.isCurrentlyOnline()
            restoreSession(lastOnline)

            networkMonitor.isOnline.collect { isOnline ->
                if (isOnline == lastOnline) return@collect
                lastOnline = isOnline

                when {
                    !isOnline && _authState.value == AppAuthState.Authenticated -> {
                        _authState.value = AppAuthState.AuthenticatedOffline
                    }
                    isOnline && _authState.value == AppAuthState.AuthenticatedOffline -> {
                        validateStoredSession()
                    }
                }
            }
        }
    }

    private suspend fun restoreSession(isOnline: Boolean) {
        authRepository.migrateStoredSession()
        if (!authRepository.hasStoredSession()) {
            _authState.value = AppAuthState.Unauthenticated
            return
        }

        if (isOnline) validateStoredSession()
        else _authState.value = AppAuthState.AuthenticatedOffline
    }

    private suspend fun validateStoredSession() {
        authRepository.getMe()
            .onSuccess {
                _authState.value = if (networkMonitor.isCurrentlyOnline()) {
                    AppAuthState.Authenticated
                } else {
                    AppAuthState.AuthenticatedOffline
                }
            }
            .onFailure { error ->
                _authState.value = if (error is ApiException && error.statusCode == 401) {
                    AppAuthState.Unauthenticated
                } else {
                    AppAuthState.AuthenticatedOffline
                }
            }
    }

    fun onAuthenticated() {
        _authState.value = AppAuthState.Authenticated
    }

    fun onLoggedOut() {
        _authState.value = AppAuthState.Unauthenticated
    }
}
