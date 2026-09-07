package com.entrymyslot.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.entrymyslot.app.screens.auth.AuthScreenViewModel
import com.entrymyslot.app.screens.home.HomeViewModel
import com.entrymyslot.app.screens.movies.MovieBookingViewModel
import com.entrymyslot.app.screens.events.EventBookingViewModel
import com.entrymyslot.app.screens.turf.TurfBookingViewModel
import com.entrymyslotbe.app.core.ApiResult
import com.entrymyslotbe.app.auth.AuthScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.delay
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackendIntegrationTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val app get() = instrumentation.targetContext.applicationContext as EntryMySlotApp
    private val backend get() = app.appContainer.backend

    @Test fun homeAndMovieSeatDataLoadFromLiveServer() = runBlocking {
        val home = HomeViewModel(app)
        instrumentation.runOnMainSync { home.loadHome("Chennai") }
        val homeState = withTimeout(60000) { home.uiState.first { !it.isLoading } }
        assertNull(homeState.errorMessage)
        assertTrue(homeState.movies.isNotEmpty())
        assertTrue(homeState.events.isNotEmpty())
        assertTrue(homeState.sports.isNotEmpty())
        val movies = backend.gateway.data { getFeaturedMovies() } as ApiResult.Success
        val id = movies.value!!.first().id!!
        val vm = MovieBookingViewModel(backend, app.appContainer.pendingCheckoutStore, "Chennai")
        instrumentation.runOnMainSync { vm.loadCinemaOptions(id) }
        val cinemaState = withTimeout(60000) { vm.uiState.first { !it.isLoading } }
        assertNull(cinemaState.errorMessage)
        assumeTrue("No live showtimes today", cinemaState.cinemas.isNotEmpty())
        val showtime = cinemaState.cinemas.first().showtimes.first()
        instrumentation.runOnMainSync { vm.loadSeatBooking(id, showtime.id) }
        val seats = withTimeout(60000) { vm.uiState.first { !it.isLoading } }
        assertNull(seats.errorMessage)
        assertTrue(seats.seatLayout!!.rows.isNotEmpty())
        assertTrue(seats.seatLayout.rows.flatMap { it.seats }.all { it.pricePaise >= 0 })
    }

    @Test fun eventOptionsComeFromServerAndTurfFailureIsVisible() = runBlocking {
        val events = backend.gateway.data { getFeaturedEvents() } as ApiResult.Success
        assumeTrue(events.value.orEmpty().isNotEmpty())
        val eventVm = EventBookingViewModel(backend, app.appContainer.pendingCheckoutStore)
        instrumentation.runOnMainSync { eventVm.loadEvent(events.value!!.first().id!!) }
        val event = withTimeout(60000) { eventVm.uiState.first { !it.isLoading } }
        assertNull(event.errorMessage)
        assertNotNull(event.event)
        assertTrue(event.options.isNotEmpty())
        val venues = backend.gateway.execute { listVenues() } as ApiResult.Success
        val resource = venues.value.data!!.first()
        val turfVm = TurfBookingViewModel(backend, app.appContainer.pendingCheckoutStore)
        instrumentation.runOnMainSync { turfVm.loadTurf(resource.id!!) }
        val turf = withTimeout(60000) { turfVm.uiState.first { !it.isLoading } }
        assertNotNull(turf.turf)
        if (turf.httpStatus != null) {
            assertTrue(turf.slots.isEmpty())
            assertFalse(turf.errorMessage.isNullOrBlank())
        }
    }

    @Test fun invalidLoginAndUnauthorizedRequestsNeverCreateSession() = runBlocking {
        assumeTrue(AuthScope.USER !in backend.sessions.state.value.authenticatedScopes)
        val vm = AuthScreenViewModel(app)
        instrumentation.runOnMainSync { vm.login("", "") }
        assertFalse(vm.uiState.value.isLoggedIn)
        assertNotNull(vm.uiState.value.errorMessage)
        instrumentation.runOnMainSync { vm.login("integration-invalid@example.invalid", "InvalidCredentials!1") }
        val state = withTimeout(60000) { vm.uiState.first { !it.isLoading } }
        assertFalse(state.isLoggedIn)
        assertNotNull(state.errorMessage)
        assertTrue(backend.gateway.data { getMe() } is ApiResult.Unauthorized)
        assertTrue(backend.gateway.execute { getMyBookings() } is ApiResult.Unauthorized)
        assertTrue(AuthScope.USER !in backend.sessions.state.value.authenticatedScopes)
    }

    @Test fun offlineGatewayReturnsNoInternetAndRecovers() = runBlocking {
        fun shell(command: String) {
            android.os.ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand(command)).use { it.readBytes() }
        }
        assumeTrue(backend.connectivity.isOnline())
        try {
            shell("svc wifi disable")
            shell("svc data disable")
            withTimeout(20000) { while (backend.connectivity.isOnline()) delay(250) }
            assertTrue(backend.home.loadHome() is ApiResult.NoInternet)
            assertTrue(backend.gateway.data { getMe() } is ApiResult.NoInternet)
        } finally {
            shell("svc wifi enable")
            shell("svc data enable")
            withTimeout(30000) { while (!backend.connectivity.isOnline()) delay(250) }
        }
        assertTrue(backend.home.loadHome() is ApiResult.Success)
    }
}
