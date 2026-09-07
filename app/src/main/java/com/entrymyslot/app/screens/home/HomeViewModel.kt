package com.entrymyslot.app.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.entrymyslot.app.EntryMySlotApp
import com.entrymyslot.app.data.mapper.toUi
import com.entrymyslot.app.data.mapper.toPromotion
import com.entrymyslotbe.app.core.ApiResult
import com.entrymyslotbe.app.data.HomeRequest
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import com.entrymyslot.app.data.model.CatalogItem
import com.entrymyslot.app.data.model.HomePromotion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HomeUiState(
    val isLoading: Boolean = false,
    val promotions: List<HomePromotion> = emptyList(),
    val events: List<CatalogItem> = emptyList(),
    val movies: List<CatalogItem> = emptyList(),
    val sports: List<CatalogItem> = emptyList(),
    val errorMessage: String? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val home = (application as EntryMySlotApp).appContainer.backend.home
    private var loadJob: Job? = null
    private var requestedCity: String? = null
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    fun loadHome(city: String) {
        if (requestedCity == city && loadJob?.isActive == true) return
        val previousState = if (requestedCity == city) _uiState.value else HomeUiState()
        requestedCity = city
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = previousState.copy(isLoading = true, errorMessage = null)
            when (val result = home.loadHome(HomeRequest(city = city.takeIf(String::isNotBlank)))) {
                is ApiResult.Success -> _uiState.value = HomeUiState(
                    promotions = result.value.ads.mapNotNull { it.toPromotion() },
                    movies = result.value.movies.mapNotNull { it.toUi() },
                    events = result.value.events.mapNotNull { it.toUi() },
                    sports = result.value.venues.mapNotNull { it.toUi() },
                    errorMessage = result.value.partialErrors.takeIf { it.isNotEmpty() }?.joinToString("\n"))
                is ApiResult.Failure -> _uiState.value = previousState.copy(isLoading = false, errorMessage = result.userMessage)
            }
        }
    }
}
