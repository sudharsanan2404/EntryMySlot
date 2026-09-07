package com.entrymyslot.app

import android.graphics.Bitmap
import androidx.activity.compose.setContent
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.test.platform.app.InstrumentationRegistry
import com.entrymyslot.app.core.theme.EntryMySlotTheme
import com.entrymyslot.app.screens.home.HomeScreen
import com.entrymyslot.app.screens.movies.MovieDetailsScreen
import com.entrymyslot.app.screens.events.EventDetailsScreen
import com.entrymyslot.app.screens.turf.TurfScreen
import com.entrymyslot.app.screens.search.SearchViewModel
import com.entrymyslot.app.screens.search.SearchResultType
import com.entrymyslot.app.screens.search.PriceFilter
import com.entrymyslotbe.app.core.ApiResult
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import java.io.File

class PublicScreenSmokeTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val app get() = compose.activity.application as EntryMySlotApp
    private val backend get() = app.appContainer.backend
    private fun screenshot(name: String) {
        compose.waitForIdle()
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        File(app.getExternalFilesDir(null), "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
    private fun waitForText(text: String) {
        compose.waitUntil(60000) { compose.onAllNodesWithText(text, substring = true).fetchSemanticsNodes().isNotEmpty() }
    }

    @Test fun publicScreensRenderLiveDataWithoutAuthenticationBypassInApp() = runBlocking {
        val home = backend.home.loadHome() as ApiResult.Success
        val movie = home.value.movies.first()
        val event = home.value.events.first()
        val resource = home.value.venues.first()
        compose.runOnUiThread { compose.activity.setContent { EntryMySlotTheme { HomeScreen(selectedCity = "Chennai") } } }
        waitForText(event.title!!)
        screenshot("integration-home")
        compose.runOnUiThread { compose.activity.setContent { EntryMySlotTheme { MovieDetailsScreen(movie.id!!, {}, {}) } } }
        waitForText(movie.title!!)
        screenshot("integration-movie")
        compose.runOnUiThread { compose.activity.setContent { EntryMySlotTheme { EventDetailsScreen(eventId = event.id!!, onBackClick = {}, onBookTicketsClick = {}) } } }
        waitForText(event.title)
        screenshot("integration-event")
        compose.runOnUiThread { compose.activity.setContent { EntryMySlotTheme { TurfScreen(sportId = resource.id!!, onBackClick = {}, onBookNowClick = {}) } } }
        waitForText(resource.name!!)
        screenshot("integration-turf")
    }

    @Test fun searchUsesServerAndRetainsDecimalPriceFiltering() = runBlocking {
        val vm = SearchViewModel(app)
        compose.runOnUiThread { vm.initialize(SearchResultType.EVENT, "Chennai") }
        val catalog = withTimeout(60000) { vm.uiState.first { !it.isLoading && it.results.isNotEmpty() } }
        assertNull(catalog.errorMessage)
        compose.runOnUiThread { vm.setPriceFilter(PriceFilter.UNDER_500) }
        val filtered = withTimeout(60000) { vm.uiState.first { !it.isLoading } }
        assertTrue(filtered.results.isNotEmpty())
        assertTrue(filtered.results.all { it.item.price.removePrefix("₹").toDouble() < 500.0 })
        compose.runOnUiThread { vm.onQueryChange("no-match-integration-query-000000") }
        val empty = withTimeout(60000) { vm.uiState.first { !it.isLoading } }
        assertTrue(empty.results.isEmpty())
        assertNull(empty.errorMessage)

        val movies = backend.gateway.data { getFeaturedMovies() } as ApiResult.Success
        val title = movies.value!!.first().title!!
        val movieSearch = SearchViewModel(app)
        compose.runOnUiThread {
            movieSearch.initialize(SearchResultType.MOVIE, "")
            movieSearch.onQueryChange(title)
        }
        val found = withTimeout(60000) { movieSearch.uiState.first { !it.isLoading } }
        assertNull(found.errorMessage)
        assertTrue("Expected movie '$title' for query '${found.query}'; returned ${found.results.map { it.item.title }}; loading=${found.isLoading}; error=${found.errorMessage}",
            found.results.any { it.item.title == title })

        val venues = backend.gateway.execute { listVenues() } as ApiResult.Success
        val venueName = venues.value.data!!.first().name!!
        val venueSearch = SearchViewModel(app)
        compose.runOnUiThread {
            venueSearch.initialize(SearchResultType.SPORT, "")
            venueSearch.onQueryChange(venueName)
        }
        val grounds = withTimeout(60000) { venueSearch.uiState.first { !it.isLoading } }
        assertNull(grounds.errorMessage)
        assertTrue("Expected venue '$venueName' for query '${grounds.query}'; returned ${grounds.results.map { it.item.title }}; loading=${grounds.isLoading}; error=${grounds.errorMessage}",
            grounds.results.any { it.item.title == venueName })
    }
}
