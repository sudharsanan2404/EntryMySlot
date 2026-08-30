package com.entrymyslot.app.screens.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.entrymyslot.app.EntryMySlotApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TestLog(
    val timestamp: String,
    val message: String,
    val type: LogType
)

enum class LogType { INFO, SUCCESS, ERROR }

data class TestUiState(
    val logs: List<TestLog> = emptyList(),
    val isRunning: Boolean = false
)

class TestViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TestUiState())
    val uiState: StateFlow<TestUiState> = _uiState.asStateFlow()

    private val container = EntryMySlotApp.instance.appContainer

    fun addLog(message: String, type: LogType = LogType.INFO) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val newLog = TestLog(time, message, type)
        _uiState.value = _uiState.value.copy(
            logs = listOf(newLog) + _uiState.value.logs
        )
    }

    private fun startTest(name: String) {
        _uiState.value = _uiState.value.copy(isRunning = true)
        addLog("Starting Test: $name...")
    }

    private fun endTest() {
        _uiState.value = _uiState.value.copy(isRunning = false)
    }

    fun testGetMe() = viewModelScope.launch {
        startTest("Get Me (Auth Check)")
        
        // Debug: Check if token exists
        val token = container.authTokenStore.accessToken.first()
        if (token.isNullOrBlank()) {
            addLog("Warning: No Access Token found in Storage. Please Login first.", LogType.ERROR)
        } else {
            addLog("Token found: ${token.take(8)}...", LogType.INFO)
        }

        container.authRepository.getMe()
            .onSuccess { addLog("Success: Logged in as ${it.email}", LogType.SUCCESS) }
            .onFailure { addLog("Error: ${it.message}", LogType.ERROR) }
        endTest()
    }

    fun testFetchEvents() = viewModelScope.launch {
        startTest("List Events")
        container.eventRepository.listEvents(limit = 5)
            .onSuccess { (list, _) -> addLog("Success: Found ${list.size} events", LogType.SUCCESS) }
            .onFailure { addLog("Error: ${it.message}", LogType.ERROR) }
        endTest()
    }

    fun testFetchMovies() = viewModelScope.launch {
        startTest("List Movies")
        container.movieRepository.listMovies(limit = 5)
            .onSuccess { (list, _) -> addLog("Success: Found ${list.size} movies", LogType.SUCCESS) }
            .onFailure { addLog("Error: ${it.message}", LogType.ERROR) }
        endTest()
    }

    fun testFetchTurfs() = viewModelScope.launch {
        startTest("List Turfs")
        container.turfRepository.listVenues(limit = 5)
            .onSuccess { (list, _) -> addLog("Success: Found ${list.size} turfs", LogType.SUCCESS) }
            .onFailure { addLog("Error: ${it.message}", LogType.ERROR) }
        endTest()
    }

    fun clearLogs() {
        _uiState.value = _uiState.value.copy(logs = emptyList())
    }
}
