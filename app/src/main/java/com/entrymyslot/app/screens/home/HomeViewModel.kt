package com.entrymyslot.app.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.entrymyslot.app.data.home.HomeApi
import com.entrymyslot.app.data.home.HomeCardDto
import com.entrymyslot.app.data.model.CatalogItem
import com.entrymyslot.app.data.model.HomePromotion
import com.entrymyslot.app.data.model.PromotionDestination
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class HomeUiState(
    val isLoading: Boolean = true,
    val promotions: List<HomePromotion> = emptyList(),
    val events: List<CatalogItem> = emptyList(),
    val movies: List<CatalogItem> = emptyList(),
    val sports: List<CatalogItem> = emptyList(),
    val errorMessage: String? = null
)

class HomeViewModel(
    private val homeApi: HomeApi
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    fun loadHome(city: String) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val response = homeApi.getHome(city.trim())
                val home = response.data
                _uiState.value = HomeUiState(
                    isLoading = false,
                    promotions = home.promotions.map { promotion ->
                        HomePromotion(
                            id = promotion.id,
                            category = promotion.category,
                            title = promotion.title,
                            subtitle = promotion.subtitle,
                            cta = promotion.cta,
                            destination = promotion.destination.toPromotionDestination(),
                            imageUrl = promotion.imageUrl
                        )
                    },
                    events = home.events.map(HomeCardDto::toCatalogItem),
                    movies = home.movies.map(HomeCardDto::toCatalogItem),
                    sports = home.sports.map(HomeCardDto::toCatalogItem)
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.toHomeMessage()
                )
            }
        }
    }

    private fun String.toPromotionDestination(): PromotionDestination =
        when (uppercase()) {
            "SPORTS" -> PromotionDestination.SPORTS
            "EVENTS" -> PromotionDestination.EVENTS
            else -> PromotionDestination.MOVIES
        }

    private fun Throwable.toHomeMessage(): String = when (this) {
        is UnknownHostException -> "No internet connection. Check your network and try again."
        is ConnectException -> "Server is unavailable right now. Please try again."
        is SocketTimeoutException -> "The server took too long to respond. Please retry."
        is HttpException -> "Unable to load Home content (server error ${code()})."
        else -> message?.takeIf { it.isNotBlank() }
            ?: "Unable to load Home content. Please try again."
    }
}

private data class HomeCatalogItem(
    override val id: String,
    override val title: String,
    override val date: String,
    override val location: String,
    override val price: String,
    override val imageUrl: String?
) : CatalogItem

private fun HomeCardDto.toCatalogItem(): CatalogItem = HomeCatalogItem(
    id = id,
    title = title,
    date = date,
    location = location,
    price = price,
    imageUrl = imageUrl
)
