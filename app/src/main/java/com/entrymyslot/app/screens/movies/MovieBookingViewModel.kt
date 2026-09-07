package com.entrymyslot.app.screens.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.entrymyslot.app.data.booking.*
import com.entrymyslot.app.data.details.MovieDetailDto
import com.entrymyslot.app.data.mapper.toDetail
import com.entrymyslotbe.app.EntryMySlotBackend
import com.entrymyslotbe.app.core.ApiResult
import com.entrymyslotbe.app.network.model.CalculatePricesRequest
import com.entrymyslotbe.app.network.model.HoldSeatsRequest
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.time.LocalDate

data class MovieCinemaOption(val cinema: CinemaDto, val showtimes: List<ShowtimeDto>)
data class MovieBookingUiState(
    val isLoading: Boolean = false, val isRefreshing: Boolean = false, val isHolding: Boolean = false,
    val isOffline: Boolean = false, val movie: MovieDetailDto? = null, val cinemas: List<MovieCinemaOption> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(), val showtime: ShowtimeDto? = null, val cinema: CinemaDto? = null,
    val seatLayout: MovieSeatLayoutDto? = null, val desiredSeatCount: Int = 1, val selectedSeatIds: Set<Int> = emptySet(),
    val holdKey: String? = null, val holdExpiresAt: String? = null, val holdSecondsRemaining: Int = 0,
    val totalPaise: Int = 0, val currency: String = "INR", val errorMessage: String? = null, val httpStatus: Int? = null
)

class MovieBookingViewModel(private val backend: EntryMySlotBackend, private val pendingCheckoutStore: PendingCheckoutStore, private val selectedCity: String = "") : ViewModel() {
    private val state = MutableStateFlow(MovieBookingUiState())
    val uiState: StateFlow<MovieBookingUiState> = state.asStateFlow()
    private var movieId = ""
    private var seatShowtimeId: Int? = null
    private var loadJob: Job? = null
    private var holdJob: Job? = null

    fun loadCinemaOptions(id: String, date: LocalDate = state.value.selectedDate) {
        movieId = id
        seatShowtimeId = null
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            state.value = MovieBookingUiState(isLoading = true, selectedDate = date)
            val movie = when (val result = backend.loadMovieForUi(id)) {
                is ApiResult.Success -> result.value?.toDetail()
                is ApiResult.Failure -> return@launch fail(result)
            }
            val cinemas = when (val result = backend.gateway.data { if (selectedCity.isBlank()) listCinemas() else getCinemasByCity(selectedCity) }) {
                is ApiResult.Success -> result.value.orEmpty().mapNotNull { it.toUiCinema() }
                is ApiResult.Failure -> return@launch fail(result)
            }
            val shows = when (val result = backend.gateway.data { listShowtimes(buildMap {
                put("movieId", id); put("date", date.toString()); if (selectedCity.isNotBlank()) put("city", selectedCity)
            }) }) {
                is ApiResult.Success -> result.value.orEmpty().mapNotNull { it.toUiShowtime() }
                is ApiResult.Failure -> return@launch fail(result)
            }
            state.value = MovieBookingUiState(movie = movie, selectedDate = date,
                cinemas = cinemas.mapNotNull { cinema -> shows.filter { it.cinemaId == cinema.id }.takeIf { it.isNotEmpty() }?.let { MovieCinemaOption(cinema, it) } })
        }
    }
    fun changeCinemaDate(date: LocalDate) = loadCinemaOptions(movieId, date)
    fun loadSeatBooking(id: String, targetShowtimeId: Int) {
        movieId = id
        seatShowtimeId = targetShowtimeId
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            state.value = MovieBookingUiState(isLoading = true)
            val movie = when (val result = backend.loadMovieForUi(id)) {
                is ApiResult.Success -> result.value?.toDetail()
                is ApiResult.Failure -> return@launch fail(result)
            }
            val show = when (val result = backend.gateway.data { getShowtime(targetShowtimeId.toString()) }) {
                is ApiResult.Success -> result.value?.toUiShowtime()
                is ApiResult.Failure -> {
                    // The live detail route rejects IDs returned by its own list route (HTTP 400).
                    if (result !is ApiResult.HttpError || result.httpCode != 400) return@launch fail(result)
                    when (val listed = backend.gateway.data { listShowtimes(mapOf("movieId" to id)) }) {
                        is ApiResult.Success -> listed.value.orEmpty().firstOrNull { it.id == targetShowtimeId.toString() }?.toUiShowtime()
                        is ApiResult.Failure -> return@launch fail(listed)
                    }
                }
            } ?: return@launch fail(ApiResult.UnexpectedError("Showtime is unavailable."))
            val cinema = when (val result = backend.gateway.data { getCinema(show.cinemaId.toString()) }) {
                is ApiResult.Success -> result.value?.toUiCinema()
                is ApiResult.Failure -> {
                    if (result !is ApiResult.HttpError || result.httpCode != 404) return@launch fail(result)
                    when (val listed = backend.gateway.data { listCinemas() }) {
                        is ApiResult.Success -> listed.value.orEmpty().firstOrNull { it.id == show.cinemaId.toString() }?.toUiCinema()
                        is ApiResult.Failure -> return@launch fail(listed)
                    }
                }
            }
            val layout = when (val result = backend.gateway.data { getSeats(targetShowtimeId.toString()) }) {
                is ApiResult.Success -> result.value?.let { data ->
                    MovieSeatLayoutDto(show.id, show.screenId, data.price ?: show.price, data.currency,
                        data.rows.orEmpty().map { row -> MovieSeatRowDto(row.rowLabel, row.seats.mapNotNull { seat ->
                            val seatId = seat.id.toIntOrNull() ?: return@mapNotNull null
                            val number = seat.number.toIntOrNull() ?: return@mapNotNull null
                            val price = seat.price ?: return@mapNotNull null
                            MovieSeatDto(seatId, number, seat.type, seat.seatCategory.orEmpty(), seat.xPosition, seat.yPosition, seat.status, price)
                        }) })
                }
                is ApiResult.Failure -> return@launch fail(result)
            }
            state.value = MovieBookingUiState(movie = movie, showtime = show, cinema = cinema, seatLayout = layout,
                errorMessage = if (layout == null) "Seat layout is unavailable." else null)
        }
    }
    fun setDesiredSeatCount(count: Int) {
        if (state.value.isHolding || state.value.holdKey != null) return
        state.value = state.value.copy(desiredSeatCount = count.coerceIn(1, 10), selectedSeatIds = emptySet(), totalPaise = 0)
    }
    fun onSeatClicked(seatId: Int) {
        val current = state.value
        if (current.isHolding || current.holdKey != null) return
        val layout = current.seatLayout ?: return
        val next = selectMovieSeatBlock(layout.rows.flatMap { row -> row.seats.map { row.rowLabel to it } }, current.selectedSeatIds, current.desiredSeatCount, seatId)
        state.value = current.copy(selectedSeatIds = next, totalPaise = layout.rows.flatMap { it.seats }.filter { it.seatId in next }.sumOf { it.pricePaise }, errorMessage = null)
    }
    fun validateSelection(): Boolean {
        val valid = state.value.selectedSeatIds.size == state.value.desiredSeatCount
        if (!valid) state.value = state.value.copy(errorMessage = "Select all ${state.value.desiredSeatCount} seats before continuing.")
        return valid
    }
    fun createHoldAndPrepareCheckout(onSuccess: () -> Unit) {
        if (state.value.isHolding || !validateSelection()) return
        val current = state.value
        val layout = current.seatLayout ?: return
        val show = current.showtime ?: return
        val cinema = current.cinema ?: return
        state.value = current.copy(isHolding = true, errorMessage = null)
        viewModelScope.launch {
            val ids = current.selectedSeatIds.sorted().map(Int::toString)
            val prices = when (val result = backend.gateway.data { calculatePrices(show.id.toString(), CalculatePricesRequest(ids)) }) {
                is ApiResult.Success -> result.value.orEmpty()
                is ApiResult.Failure -> return@launch fail(result)
            }
            if (prices.mapNotNull { it.seatId }.toSet() != ids.toSet() || prices.any { it.finalPrice == null })
                return@launch fail(ApiResult.UnexpectedError("The server did not return prices for every selected seat."))
            val hold = when (val result = backend.gateway.data { holdSeats(show.id.toString(), HoldSeatsRequest(ids, show.id.toString())) }) {
                is ApiResult.Success -> result.value
                is ApiResult.Failure -> return@launch fail(result)
            }
            if (hold?.success != true || hold.holdKey.isNullOrBlank() || hold.holdExpiresAt.isNullOrBlank())
                return@launch fail(ApiResult.UnexpectedError("The selected seats could not be held."))
            val subtotal = prices.sumOf { it.finalPrice!! }
            val labels = layout.rows.flatMap { row -> row.seats.filter { it.seatId in current.selectedSeatIds }.map { "${row.rowLabel}${it.seatNumber}" } }
            val bill = AuthoritativeBillDto("MOVIE", ids.size, subtotal, currency = layout.currency)
            pendingCheckoutStore.save(PendingMovieCheckout(movieId, current.movie?.title.orEmpty(), show.id, cinema.id, cinema.name, show.showDatetime,
                current.selectedSeatIds.sorted(), labels, hold.holdKey, hold.holdExpiresAt, subtotal, layout.currency, bill))
            state.value = state.value.copy(isHolding = false, holdKey = hold.holdKey, holdExpiresAt = hold.holdExpiresAt, totalPaise = subtotal)
            monitorHold(hold.holdKey, hold.holdExpiresAt)
            onSuccess()
        }
    }
    private fun monitorHold(key: String, expiresAt: String) {
        holdJob?.cancel()
        holdJob = viewModelScope.launch {
            val expiry = runCatching { Instant.parse(expiresAt) }.getOrNull() ?: return@launch
            while (pendingCheckoutStore.payment == null) {
                val seconds = java.time.Duration.between(Instant.now(), expiry).seconds.coerceAtLeast(0).toInt()
                state.value = state.value.copy(holdSecondsRemaining = seconds)
                if (seconds == 0) {
                    if ((pendingCheckoutStore.current.value as? PendingMovieCheckout)?.holdKey == key) pendingCheckoutStore.clear()
                    state.value = state.value.copy(holdKey = null, errorMessage = "Seat hold expired. Select your seats again.")
                    break
                }
                delay(1000)
            }
        }
    }
    fun releaseAndGoBack(onReleased: () -> Unit) {
        if (state.value.isHolding) return
        holdJob?.cancel()
        viewModelScope.launch {
            val key = state.value.holdKey
            if (key != null) {
                val result = backend.gateway.data { releaseSeats(key) }
                if (result is ApiResult.Failure) return@launch fail(result)
            }
            pendingCheckoutStore.clear()
            state.value = state.value.copy(holdKey = null)
            onReleased()
        }
    }
    fun retry() { val show = seatShowtimeId; if (show == null) loadCinemaOptions(movieId) else loadSeatBooking(movieId, show) }
    private fun fail(result: ApiResult.Failure) {
        state.value = state.value.copy(isLoading = false, isHolding = false, isOffline = result is ApiResult.NoInternet, errorMessage = result.userMessage, httpStatus = (result as? ApiResult.HttpError)?.httpCode)
    }
}

private fun com.entrymyslotbe.app.network.model.Cinema.toUiCinema(): CinemaDto? = id?.toIntOrNull()?.let {
    CinemaDto(it, name.orEmpty(), address.orEmpty(), city.orEmpty(), state.orEmpty(), facilities.orEmpty())
}
private fun com.entrymyslotbe.app.network.model.Showtime.toUiShowtime(): ShowtimeDto? {
    return ShowtimeDto(id?.toIntOrNull() ?: return null, movieId?.toIntOrNull() ?: return null,
        cinemaId?.toIntOrNull() ?: return null, screenId?.toIntOrNull() ?: return null,
        startTime ?: return null, endTime.orEmpty(), language.orEmpty(), format.orEmpty(), price ?: return null,
        currency, totalSeats ?: 0, availableSeats ?: 0, status = status.orEmpty())
}
internal fun selectMovieSeatBlock(rows: List<Pair<String, MovieSeatDto>>, current: Set<Int>, desiredCount: Int, clickedSeatId: Int): Set<Int> {
    val clickedPair = rows.firstOrNull { it.second.seatId == clickedSeatId } ?: return current
    val clicked = clickedPair.second
    if (clickedSeatId in current) return current - clickedSeatId
    if (current.size >= desiredCount || clicked.status != "available") return current
    val selectedTier = rows.firstOrNull { it.second.seatId in current }?.second?.tierKey
    if (selectedTier != null && selectedTier != clicked.tierKey) return current
    val sameRow = rows.filter { it.first == clickedPair.first }.map { it.second }.sortedBy { it.seatNumber }
    val selectable = sameRow.filter { it.tierKey == clicked.tierKey && (it.status == "available" || it.seatId in current) }
    val index = selectable.indexOfFirst { it.seatId == clickedSeatId }; if (index < 0) return current
    val ordered = selectable.drop(index) + selectable.take(index).asReversed()
    return current + ordered.filter { it.seatId !in current }.take(desiredCount - current.size).map { it.seatId }
}
