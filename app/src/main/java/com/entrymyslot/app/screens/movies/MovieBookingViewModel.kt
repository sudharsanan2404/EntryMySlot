package com.entrymyslot.app.screens.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.entrymyslot.app.core.network.NetworkMonitor
import com.entrymyslot.app.data.booking.BookingApi
import com.entrymyslot.app.data.booking.CinemaDto
import com.entrymyslot.app.data.booking.MovieSeatDto
import com.entrymyslot.app.data.booking.MovieSeatHoldRequest
import com.entrymyslot.app.data.booking.MovieSeatLayoutDto
import com.entrymyslot.app.data.booking.MovieSeatLayoutResponse
import com.entrymyslot.app.data.booking.PendingCheckoutStore
import com.entrymyslot.app.data.booking.PendingMovieCheckout
import com.entrymyslot.app.data.booking.CinemaListResponse
import com.entrymyslot.app.data.booking.ShowtimeDto
import com.entrymyslot.app.data.booking.ShowtimeListResponse
import com.entrymyslot.app.data.details.DetailsApi
import com.entrymyslot.app.data.details.MovieDetailDto
import com.entrymyslot.app.data.details.MovieDetailResponse
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class MovieCinemaOption(
    val cinema: CinemaDto,
    val showtimes: List<ShowtimeDto>
)

data class MovieBookingUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isHolding: Boolean = false,
    val isOffline: Boolean = false,
    val movie: MovieDetailDto? = null,
    val cinemas: List<MovieCinemaOption> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(),
    val showtime: ShowtimeDto? = null,
    val cinema: CinemaDto? = null,
    val seatLayout: MovieSeatLayoutDto? = null,
    val desiredSeatCount: Int = 1,
    val selectedSeatIds: Set<Int> = emptySet(),
    val holdKey: String? = null,
    val holdExpiresAt: String? = null,
    val holdSecondsRemaining: Int = 0,
    val totalPaise: Int = 0,
    val currency: String = "INR",
    val errorMessage: String? = null,
    val httpStatus: Int? = null
)

private data class MovieSeatLoadResult(
    val movie: MovieDetailResponse,
    val cinemas: CinemaListResponse,
    val showtimes: ShowtimeListResponse,
    val layout: MovieSeatLayoutResponse
)

class MovieBookingViewModel(
    private val bookingApi: BookingApi,
    private val detailsApi: DetailsApi,
    private val networkMonitor: NetworkMonitor,
    private val pendingCheckoutStore: PendingCheckoutStore
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        MovieBookingUiState(isOffline = !networkMonitor.isCurrentlyOnline())
    )
    val uiState: StateFlow<MovieBookingUiState> = _uiState.asStateFlow()

    private var movieId: String? = null
    private var showtimeId: Int? = null
    private var loadJob: Job? = null
    private var countdownJob: Job? = null

    init {
        viewModelScope.launch {
            var wasOnline = networkMonitor.isCurrentlyOnline()
            networkMonitor.isOnline.collect { online ->
                val reconnect = online && !wasOnline
                wasOnline = online
                _uiState.value = _uiState.value.copy(isOffline = !online)
                if (reconnect) retry()
            }
        }
    }

    fun loadCinemaOptions(id: String, date: LocalDate = _uiState.value.selectedDate) {
        movieId = id
        showtimeId = null
        loadJob?.cancel()
        if (!networkMonitor.isCurrentlyOnline()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isOffline = true,
                errorMessage = "No internet connection. Reconnect and try again."
            )
            return
        }
        loadJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                selectedDate = date,
                errorMessage = null,
                httpStatus = null
            )
            try {
                val numericMovieId = id.toIntOrNull() ?: error("Invalid movie ID")
                val (movieResponse, cinemaResponse, showtimeResponse) = coroutineScope {
                    val movieDeferred = async { detailsApi.getMovieDetails(id) }
                    val cinemasDeferred = async { bookingApi.getCinemas() }
                    val showtimesDeferred = async { bookingApi.getMovieShowtimes(numericMovieId) }
                    Triple(movieDeferred.await(), cinemasDeferred.await(), showtimesDeferred.await())
                }
                val datedShowtimes = showtimeResponse.data.filter { it.localDate() == date }
                val options = cinemaResponse.data.mapNotNull { cinema ->
                    val times = datedShowtimes.filter { it.cinemaId == cinema.id }
                    if (times.isEmpty()) null else MovieCinemaOption(cinema, times)
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isOffline = false,
                    movie = movieResponse.data,
                    cinemas = options,
                    errorMessage = null,
                    httpStatus = 200
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                setError(error)
            }
        }
    }

    fun changeCinemaDate(date: LocalDate) {
        movieId?.let { loadCinemaOptions(it, date) }
    }

    fun loadSeatBooking(id: String, targetShowtimeId: Int) {
        movieId = id
        showtimeId = targetShowtimeId
        loadJob?.cancel()
        if (!networkMonitor.isCurrentlyOnline()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isOffline = true,
                errorMessage = "No internet connection. Reconnect and try again."
            )
            return
        }
        loadJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, httpStatus = null)
            try {
                val numericMovieId = id.toIntOrNull() ?: error("Invalid movie ID")
                val loaded = coroutineScope {
                    val movieDeferred = async { detailsApi.getMovieDetails(id) }
                    val cinemasDeferred = async { bookingApi.getCinemas() }
                    val showtimesDeferred = async { bookingApi.getMovieShowtimes(numericMovieId) }
                    val layoutDeferred = async { bookingApi.getMovieSeatLayout(targetShowtimeId) }
                    MovieSeatLoadResult(
                        movie = movieDeferred.await(),
                        cinemas = cinemasDeferred.await(),
                        showtimes = showtimesDeferred.await(),
                        layout = layoutDeferred.await()
                    )
                }
                val selectedShowtime = loaded.showtimes.data.firstOrNull { it.id == targetShowtimeId }
                    ?: error("Showtime is no longer available")
                val selectedCinema = loaded.cinemas.data.firstOrNull { it.id == selectedShowtime.cinemaId }
                    ?: error("Cinema is no longer available")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isOffline = false,
                    movie = loaded.movie.data,
                    showtime = selectedShowtime,
                    cinema = selectedCinema,
                    seatLayout = loaded.layout.data,
                    currency = loaded.layout.data.currency,
                    errorMessage = null,
                    httpStatus = 200
                )
                recoverCurrentHold(targetShowtimeId)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                setError(error)
            }
        }
    }

    fun setDesiredSeatCount(count: Int) {
        val safeCount = count.coerceIn(1, 10)
        if (safeCount == _uiState.value.desiredSeatCount) return
        if (_uiState.value.holdKey != null) return
        _uiState.value = _uiState.value.copy(desiredSeatCount = safeCount, selectedSeatIds = emptySet(), totalPaise = 0)
    }

    fun onSeatClicked(seatId: Int) {
        val state = _uiState.value
        val layout = state.seatLayout ?: return
        if (state.isHolding || state.holdKey != null) return
        val next = selectMovieSeatBlock(
            rows = layout.rows.flatMap { row -> row.seats.map { row.rowLabel to it } },
            current = state.selectedSeatIds,
            desiredCount = state.desiredSeatCount,
            clickedSeatId = seatId
        )
        if (next == state.selectedSeatIds) return
        val total = layout.rows.flatMap { it.seats }.filter { it.seatId in next }.sumOf { it.pricePaise }
        _uiState.value = state.copy(selectedSeatIds = next, totalPaise = total, errorMessage = null)
    }

    fun validateSelection(): Boolean {
        val state = _uiState.value
        if (state.selectedSeatIds.size != state.desiredSeatCount) {
            _uiState.value = state.copy(errorMessage = "Select all ${state.desiredSeatCount} seats before continuing.")
            return false
        }
        return true
    }

    fun createHoldAndPrepareCheckout(onSuccess: () -> Unit) {
        if (!validateSelection() || _uiState.value.isHolding) return
        val showId = showtimeId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isHolding = true, errorMessage = null)
            try {
                val response = bookingApi.holdMovieSeats(MovieSeatHoldRequest(showId, _uiState.value.selectedSeatIds.sorted()))
                applyHoldAndSave(response.data)
                onSuccess()
            } catch (error: CancellationException) { throw error }
            catch (error: Throwable) {
                _uiState.value = _uiState.value.copy(isHolding = false)
                setError(error)
                refreshSeatLayout()
            }
        }
    }

    private fun applyHoldAndSave(hold: com.entrymyslot.app.data.booking.MovieSeatHoldDto) {
        val state = _uiState.value
        val layout = state.seatLayout ?: return
        val showtime = state.showtime ?: return
        val cinema = state.cinema ?: return
        val bill = hold.bill ?: return
        val seatLabels = layout.rows.flatMap { row ->
            row.seats.filter { it.seatId in hold.heldSeatIds }.map { "${row.rowLabel}${it.seatNumber}" }
        }
        _uiState.value = state.copy(isHolding = false, selectedSeatIds = hold.heldSeatIds.toSet(), holdKey = hold.holdKey,
            holdExpiresAt = hold.holdExpiresAt, holdSecondsRemaining = hold.ttlSeconds, totalPaise = bill.totalPaise,
            currency = bill.currency, errorMessage = null, httpStatus = 200)
        pendingCheckoutStore.save(
            PendingMovieCheckout(
                itemId = movieId.orEmpty(),
                movieTitle = state.movie?.title.orEmpty(),
                showtimeId = showtime.id,
                cinemaId = cinema.id,
                cinemaName = cinema.name,
                showDatetime = showtime.showDatetime,
                seatIds = hold.heldSeatIds.sorted(),
                seatLabels = seatLabels,
                holdKey = hold.holdKey,
                holdExpiresAt = hold.holdExpiresAt,
                totalPaise = bill.totalPaise,
                currency = bill.currency,
                bill = bill
            )
        )
        startCountdown()
    }

    fun releaseAndGoBack(onReleased: () -> Unit) {
        val holdKey = _uiState.value.holdKey
        if (holdKey == null) {
            onReleased()
            return
        }
        viewModelScope.launch {
            runCatching { bookingApi.releaseMovieSeats(holdKey) }
            clearHoldState()
            onReleased()
        }
    }

    fun retry() {
        val id = movieId ?: return
        val showId = showtimeId
        if (showId == null) loadCinemaOptions(id) else loadSeatBooking(id, showId)
    }

    private fun recoverCurrentHold(showId: Int) {
        viewModelScope.launch {
            try {
                val current = bookingApi.getCurrentMovieHold(showId).data
                if (current.active && current.expiresAt != null && current.bill != null && current.holdKey != null) {
                    applyHoldAndSave(com.entrymyslot.app.data.booking.MovieSeatHoldDto(true, current.seatIds, emptyList(), current.expiresAt,
                        current.holdKey, current.ttlSeconds, current.bill))
                }
            } catch (_: Throwable) { /* Availability remains usable if recovery is unavailable. */ }
        }
    }

    private fun releaseHold(clearSelection: Boolean) {
        val key = _uiState.value.holdKey
        if (clearSelection) clearHoldState()
        if (key != null) {
            viewModelScope.launch { runCatching { bookingApi.releaseMovieSeats(key) } }
        }
    }

    private fun clearHoldState() {
        countdownJob?.cancel()
        _uiState.value = _uiState.value.copy(
            selectedSeatIds = emptySet(),
            holdKey = null,
            holdExpiresAt = null,
            holdSecondsRemaining = 0,
            totalPaise = 0,
            isHolding = false
        )
    }

    private fun refreshSeatLayout() {
        val showId = showtimeId ?: return
        viewModelScope.launch {
            try {
                val response = bookingApi.getMovieSeatLayout(showId)
                _uiState.value = _uiState.value.copy(
                    seatLayout = response.data,
                    isRefreshing = false,
                    isOffline = false
                )
            } catch (_: CancellationException) {
                throw CancellationException()
            } catch (_: Throwable) {
                // Keep current layout during a background refresh failure.
            }
        }
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (true) {
                val expiresAt = _uiState.value.holdExpiresAt ?: break
                val remaining = runCatching {
                    ((Instant.parse(expiresAt).toEpochMilli() - System.currentTimeMillis() + 999) / 1000).toInt()
                }.getOrDefault(0).coerceAtLeast(0)
                _uiState.value = _uiState.value.copy(holdSecondsRemaining = remaining)
                if (remaining == 0) {
                    clearHoldState()
                    _uiState.value = _uiState.value.copy(errorMessage = "Your five-minute seat hold expired. Please select again.")
                    refreshSeatLayout()
                    break
                }
                delay(1_000)
            }
        }
    }

    private fun setError(error: Throwable) {
        val status = (error as? HttpException)?.code()
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            isRefreshing = false,
            isOffline = error.isNetworkFailure() || !networkMonitor.isCurrentlyOnline(),
            errorMessage = when {
                error is HttpException && error.code() == 401 -> "Your session expired. Sign in again."
                error is HttpException && error.code() == 409 -> "Some seats were just reserved by another user. The layout has been refreshed."
                error.isNetworkFailure() -> "No internet connection. Reconnect and try again."
                error.message?.isNotBlank() == true -> error.message
                else -> "Unable to load movie booking. Try again."
            },
            httpStatus = status
        )
    }
}

internal fun selectMovieSeatBlock(
    rows: List<Pair<String, MovieSeatDto>>,
    current: Set<Int>,
    desiredCount: Int,
    clickedSeatId: Int
): Set<Int> {
    val clickedPair = rows.firstOrNull { it.second.seatId == clickedSeatId } ?: return current
    val clicked = clickedPair.second
    if (clickedSeatId in current) return current - clickedSeatId
    if (current.size >= desiredCount || clicked.status != "available") return current

    val selectedTier = rows.firstOrNull { it.second.seatId in current }?.second?.tierKey
    if (selectedTier != null && selectedTier != clicked.tierKey) return current

    val sameRow = rows.filter { it.first == clickedPair.first }
        .map { it.second }
        .sortedBy { it.seatNumber }
    val selectable = sameRow.filter { it.status == "available" || it.seatId in current }
    val clickedIndex = selectable.indexOfFirst { it.seatId == clickedSeatId }
    if (clickedIndex < 0) return current

    var start = clickedIndex
    var end = clickedIndex
    while (start > 0 && selectable[start].seatNumber - selectable[start - 1].seatNumber == 1) start--
    while (end < selectable.lastIndex && selectable[end + 1].seatNumber - selectable[end].seatNumber == 1) end++
    val run = selectable.subList(start, end + 1).filter { it.seatId !in current }
    val remaining = desiredCount - current.size
    if (remaining <= 0) return current

    val clickedRunIndex = run.indexOfFirst { it.seatId == clickedSeatId }.coerceAtLeast(0)
    val ordered = buildList {
        addAll(run.drop(clickedRunIndex))
        addAll(run.take(clickedRunIndex).asReversed())
    }
    return current + ordered.take(remaining).map { it.seatId }
}

private fun ShowtimeDto.localDate(): LocalDate =
    Instant.parse(showDatetime).atZone(ZoneId.systemDefault()).toLocalDate()

private fun Throwable.isNetworkFailure(): Boolean =
    this is UnknownHostException || this is ConnectException || this is SocketTimeoutException
