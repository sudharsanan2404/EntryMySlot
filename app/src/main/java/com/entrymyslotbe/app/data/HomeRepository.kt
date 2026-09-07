package com.entrymyslotbe.app.data

import com.entrymyslotbe.app.core.ApiResult
import com.entrymyslotbe.app.network.BackendGateway
import com.entrymyslotbe.app.network.model.ApiResponse
import com.entrymyslotbe.app.network.model.Event
import com.entrymyslotbe.app.network.model.Movie
import com.entrymyslotbe.app.network.model.PaginatedResponse
import com.entrymyslotbe.app.network.model.SponsoredResult
import com.entrymyslotbe.app.network.model.Venue
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.util.concurrent.ConcurrentHashMap

data class HomeRequest(
    val city: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
)

data class HomeSnapshot(
    val movies: List<Movie> = emptyList(),
    val events: List<Event> = emptyList(),
    val venues: List<Venue> = emptyList(),
    val ads: List<SponsoredResult> = emptyList(),
    // The live server has no public home-banner endpoint yet. This stable field is
    // intentionally ready for the UI and can be filled by a provider tomorrow.
    val banners: List<Any> = emptyList(),
    val extraSections: Map<String, List<Any>> = emptyMap(),
    val partialErrors: List<String> = emptyList(),
)

fun interface HomeSectionProvider {
    suspend fun load(request: HomeRequest): List<Any>
}

class HomeRepository(
    private val gateway: BackendGateway,
) {
    private val providers = ConcurrentHashMap<String, HomeSectionProvider>()

    fun registerSection(name: String, provider: HomeSectionProvider) {
        providers[name] = provider
    }

    fun removeSection(name: String) {
        providers.remove(name)
    }

    suspend fun loadHome(request: HomeRequest = HomeRequest()): ApiResult<HomeSnapshot> = coroutineScope {
        val cityFilter = request.city?.takeIf(String::isNotBlank)?.let { mapOf("city" to it) }.orEmpty()
        val sponsoredParams = buildMap {
            put("placement", "home")
            put("limit", "10")
            request.city?.takeIf(String::isNotBlank)?.let { put("locationKey", it) }
        }

        val moviesCall = async { gateway.execute { getFeaturedMovies() } }
        val eventsCall = async { gateway.execute { getFeaturedEvents() } }
        val venuesCall = async { gateway.execute { listVenues(cityFilter) } }
        val adsCall = async { gateway.execute { getSponsoredResults(sponsoredParams) } }
        val extensionCalls = providers.mapValues { (_, provider) ->
            async { runCatching { provider.load(request) }.getOrElse { emptyList() } }
        }

        val moviesResult = moviesCall.await()
        val eventsResult = eventsCall.await()
        val venuesResult = venuesCall.await()
        val adsResult = adsCall.await()
        val allResults = listOf(moviesResult, eventsResult, venuesResult, adsResult)

        if (allResults.all { it is ApiResult.Failure }) {
            return@coroutineScope allResults.first() as ApiResult.Failure
        }

        val errors = buildList {
            addFailure("movies", moviesResult)
            addFailure("events", eventsResult)
            addFailure("venues", venuesResult)
            addFailure("ads", adsResult)
        }
        ApiResult.Success(
            value = HomeSnapshot(
                movies = moviesResult.envelopeList(),
                events = eventsResult.envelopeList(),
                venues = venuesResult.pageList(),
                ads = (adsResult as? ApiResult.Success)?.value?.data?.sponsored.orEmpty(),
                extraSections = extensionCalls.mapValues { it.value.await() },
                partialErrors = errors,
            ),
            httpCode = 200,
            message = if (errors.isEmpty()) "Home data loaded." else "Home data loaded partially.",
        )
    }

    private fun MutableList<String>.addFailure(name: String, result: ApiResult<*>) {
        if (result is ApiResult.Failure) add("$name: ${result.userMessage}")
    }

    private fun <T> ApiResult<ApiResponse<List<T>>>.envelopeList(): List<T> =
        (this as? ApiResult.Success)?.value?.data.orEmpty()

    private fun <T> ApiResult<PaginatedResponse<T>>.pageList(): List<T> =
        (this as? ApiResult.Success)?.value?.data.orEmpty()
}
