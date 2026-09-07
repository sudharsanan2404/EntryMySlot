package com.entrymyslot.app.screens.events

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.entrymyslot.app.EntryMySlotApp
import com.entrymyslot.app.data.mapper.toUi
import com.entrymyslot.app.data.model.Event
import com.entrymyslotbe.app.core.ApiResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class EventDetailUiState(val isLoading: Boolean = false, val event: Event? = null, val isOffline: Boolean = false, val errorMessage: String? = null, val httpStatus: Int? = null)
class EventViewModel(application: Application) : AndroidViewModel(application) {
    private val backend = (application as EntryMySlotApp).appContainer.backend
    private val state = MutableStateFlow(EventDetailUiState())
    val uiState: StateFlow<EventDetailUiState> = state.asStateFlow()
    private var id = ""
    private var job: Job? = null
    fun loadEvent(id: String, force: Boolean = false) {
        if (!force && this.id == id && (job?.isActive == true || state.value.event != null)) return
        this.id = id
        job?.cancel()
        job = viewModelScope.launch {
            state.value = EventDetailUiState(isLoading = true)
            when (val result = backend.gateway.data { getEvent(id) }) {
                is ApiResult.Success -> {
                    val item = result.value?.toUi()
                    state.value = EventDetailUiState(event = item, errorMessage = if (item == null) "Details are unavailable." else null)
                }
                is ApiResult.Failure -> state.value = EventDetailUiState(isOffline = result is ApiResult.NoInternet, errorMessage = result.userMessage, httpStatus = (result as? ApiResult.HttpError)?.httpCode)
            }
        }
    }
    fun retry() = loadEvent(id, force = true)
}
