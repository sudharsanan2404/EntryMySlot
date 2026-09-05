package com.entrymyslot.app.screens.home

import androidx.lifecycle.ViewModel
import com.entrymyslot.app.data.FakeData
import com.entrymyslot.app.data.model.CatalogItem
import com.entrymyslot.app.data.model.HomePromotion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HomeUiState(
    val isLoading: Boolean = false,
    val promotions: List<HomePromotion> = FakeData.promotions,
    val events: List<CatalogItem> = FakeData.events,
    val movies: List<CatalogItem> = FakeData.movies,
    val sports: List<CatalogItem> = FakeData.turfs,
    val errorMessage: String? = null
)

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    fun loadHome(@Suppress("UNUSED_PARAMETER") city: String) { _uiState.value = HomeUiState() }
}
