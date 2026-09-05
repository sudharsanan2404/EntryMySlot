package com.entrymyslot.app.screens.movies

import androidx.lifecycle.ViewModel
import com.entrymyslot.app.data.FakeData
import com.entrymyslot.app.data.booking.*
import com.entrymyslot.app.data.details.MovieDetailDto
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

class MovieBookingViewModel(private val pendingCheckoutStore: PendingCheckoutStore) : ViewModel() {
    private val state = MutableStateFlow(MovieBookingUiState()); val uiState: StateFlow<MovieBookingUiState> = state.asStateFlow()
    private var movieId = ""

    fun loadCinemaOptions(id: String, date: LocalDate = state.value.selectedDate) {
        movieId = id
        val movie = movieDto(id)
        val cinemas = FakeData.cinemas.mapIndexed { index, cinema ->
            val dto = CinemaDto(index + 1, cinema.name, cinema.location, "Chennai")
            MovieCinemaOption(dto, listOf(10, 14, 18, 21).mapIndexed { timeIndex, hour -> showtime(index * 10 + timeIndex + 1, index + 1, date, hour) })
        }
        state.value = MovieBookingUiState(movie = movie, cinemas = cinemas, selectedDate = date)
    }

    fun changeCinemaDate(date: LocalDate) = loadCinemaOptions(movieId, date)

    fun loadSeatBooking(id: String, targetShowtimeId: Int) {
        movieId = id
        val cinemaIndex = ((targetShowtimeId - 1) / 10).coerceIn(0, FakeData.cinemas.lastIndex)
        val cinema = FakeData.cinemas[cinemaIndex]
        val cinemaDto = CinemaDto(cinemaIndex + 1, cinema.name, cinema.location, "Chennai")
        val show = showtime(targetShowtimeId, cinemaDto.id, LocalDate.now(), 18)
        val rows = ('A'..'G').mapIndexed { rowIndex, label ->
            MovieSeatRowDto(label.toString(), (1..8).map { number ->
                val seatId = rowIndex * 8 + number
                MovieSeatDto(seatId, number, "regular", "standard", status = if (seatId in setOf(2, 12, 19, 35)) "booked" else "available", pricePaise = 22000)
            })
        }
        state.value = MovieBookingUiState(movie = movieDto(id), showtime = show, cinema = cinemaDto, seatLayout = MovieSeatLayoutDto(show.id, 1, 22000, "INR", rows))
    }

    fun setDesiredSeatCount(count: Int) { state.value = state.value.copy(desiredSeatCount = count.coerceIn(1, 10), selectedSeatIds = emptySet(), totalPaise = 0) }
    fun onSeatClicked(seatId: Int) {
        val current = state.value; val layout = current.seatLayout ?: return
        val next = selectMovieSeatBlock(layout.rows.flatMap { row -> row.seats.map { row.rowLabel to it } }, current.selectedSeatIds, current.desiredSeatCount, seatId)
        state.value = current.copy(selectedSeatIds = next, totalPaise = layout.rows.flatMap { it.seats }.filter { it.seatId in next }.sumOf { it.pricePaise }, errorMessage = null)
    }
    fun validateSelection(): Boolean {
        val valid = state.value.selectedSeatIds.size == state.value.desiredSeatCount
        if (!valid) state.value = state.value.copy(errorMessage = "Select all ${state.value.desiredSeatCount} seats before continuing.")
        return valid
    }
    fun createHoldAndPrepareCheckout(onSuccess: () -> Unit) {
        if (!validateSelection()) return
        val current = state.value; val layout = current.seatLayout ?: return; val show = current.showtime ?: return; val cinema = current.cinema ?: return
        val labels = layout.rows.flatMap { row -> row.seats.filter { it.seatId in current.selectedSeatIds }.map { "${row.rowLabel}${it.seatNumber}" } }
        val bill = previewBill("MOVIE", current.selectedSeatIds.size, current.totalPaise)
        pendingCheckoutStore.save(PendingMovieCheckout(movieId, current.movie?.title.orEmpty(), show.id, cinema.id, cinema.name, show.showDatetime, current.selectedSeatIds.sorted(), labels, "preview-hold", Instant.now().plusSeconds(300).toString(), bill.totalPaise, "INR", bill))
        onSuccess()
    }
    fun releaseAndGoBack(onReleased: () -> Unit) { pendingCheckoutStore.clear(); onReleased() }
    fun retry() { if (state.value.showtime == null) loadCinemaOptions(movieId) else loadSeatBooking(movieId, state.value.showtime!!.id) }

    private fun movieDto(id: String): MovieDetailDto? = FakeData.getMovieById(id)?.let { movie -> MovieDetailDto(id.filter(Char::isDigit).toIntOrNull() ?: 1, movie.title, movie.description, listOf(movie.genre), movie.language, rating = movie.rating, releaseDate = movie.releaseDate, status = "NOW_SHOWING") }
    private fun showtime(id: Int, cinemaId: Int, date: LocalDate, hour: Int) = ShowtimeDto(id, 1, cinemaId, 1, "${date}T${hour.toString().padStart(2, '0')}:00:00Z", "${date}T${(hour + 3).coerceAtMost(23).toString().padStart(2, '0')}:00:00Z", "English", "2D", 22000, totalSeats = 56, availableSeats = 42, status = "active")
}

internal fun selectMovieSeatBlock(rows: List<Pair<String, MovieSeatDto>>, current: Set<Int>, desiredCount: Int, clickedSeatId: Int): Set<Int> {
    val clickedPair = rows.firstOrNull { it.second.seatId == clickedSeatId } ?: return current
    val clicked = clickedPair.second
    if (clickedSeatId in current) return current - clickedSeatId
    if (current.size >= desiredCount || clicked.status != "available") return current
    val selectedTier = rows.firstOrNull { it.second.seatId in current }?.second?.tierKey
    if (selectedTier != null && selectedTier != clicked.tierKey) return current
    val sameRow = rows.filter { it.first == clickedPair.first }.map { it.second }.sortedBy { it.seatNumber }
    val selectable = sameRow.filter { it.status == "available" || it.seatId in current }
    val index = selectable.indexOfFirst { it.seatId == clickedSeatId }; if (index < 0) return current
    val ordered = selectable.drop(index) + selectable.take(index).asReversed()
    return current + ordered.filter { it.seatId !in current }.take(desiredCount - current.size).map { it.seatId }
}

internal fun previewBill(domain: String, quantity: Int, subtotal: Int): AuthoritativeBillDto {
    val fee = 3000; val gst = 1800
    return AuthoritativeBillDto(domain, quantity, subtotal, taxableAmountPaise = subtotal + fee, cgstPaise = gst / 2, sgstPaise = gst / 2, gstTotalPaise = gst, platformFeePaise = fee, totalPaise = subtotal + fee + gst, currency = "INR", calculatedAt = Instant.now().toString())
}
