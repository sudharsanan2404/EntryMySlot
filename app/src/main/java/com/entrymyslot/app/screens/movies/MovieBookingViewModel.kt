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
    val showtimes: List<ShowtimeDto> = emptyList(),
    val seatLayout: MovieSeatLayoutDto? = null, val desiredSeatCount: Int = 1, val selectedSeatIds: Set<Int> = emptySet(),
    val holdKey: String? = null, val holdExpiresAt: String? = null, val holdSecondsRemaining: Int = 0,
    val totalPaise: Int = 0, val currency: String = "INR", val bill: AuthoritativeBillDto? = null,
    val errorMessage: String? = null, val httpStatus: Int? = null
)

class MovieBookingViewModel(private val pendingCheckoutStore: PendingCheckoutStore) : ViewModel() {
    private val state = MutableStateFlow(MovieBookingUiState()); val uiState: StateFlow<MovieBookingUiState> = state.asStateFlow()
    private var movieId = ""

    fun loadCinemaOptions(id: String, date: LocalDate = state.value.selectedDate) {
        movieId = id
        val movie = movieDto(id)
        val cinemas = FakeData.cinemas.mapIndexed { index, cinema ->
            val dto = CinemaDto(index + 1, cinema.name, cinema.location, "Chennai", facilities = cinema.facilities)
            MovieCinemaOption(dto, listOf(10, 14, 18, 21).mapIndexed { timeIndex, hour -> showtime(index * 10 + timeIndex + 1, index + 1, date, hour) })
        }
        state.value = MovieBookingUiState(movie = movie, cinemas = cinemas, selectedDate = date)
    }

    fun changeCinemaDate(date: LocalDate) = loadCinemaOptions(movieId, date)

    fun loadSeatBooking(id: String, targetShowtimeId: Int) {
        movieId = id
        val cinemaIndex = ((targetShowtimeId - 1) / 10).coerceIn(0, FakeData.cinemas.lastIndex)
        val cinema = FakeData.cinemas[cinemaIndex]
        val cinemaDto = CinemaDto(cinemaIndex + 1, cinema.name, cinema.location, "Chennai", facilities = cinema.facilities)
        val localShowtimes = listOf(10, 14, 18, 21).mapIndexed { index, hour ->
            showtime(cinemaIndex * 10 + index + 1, cinemaDto.id, LocalDate.now(), hour)
        }
        val show = localShowtimes.firstOrNull { it.id == targetShowtimeId }
            ?: showtime(targetShowtimeId, cinemaDto.id, LocalDate.now(), 18)
        val rows = ('A'..'G').mapIndexed { rowIndex, label ->
            MovieSeatRowDto(label.toString(), (1..8).map { number ->
                val seatId = rowIndex * 8 + number
                MovieSeatDto(seatId, number, "regular", "standard", status = if (seatId in setOf(2, 12, 19, 35)) "booked" else "available", pricePaise = 22000)
            })
        }
        state.value = MovieBookingUiState(movie = movieDto(id), showtime = show, cinema = cinemaDto,
            showtimes = localShowtimes, seatLayout = MovieSeatLayoutDto(show.id, 1, 22000, "INR", rows))
    }

    fun setDesiredSeatCount(count: Int) { state.value = state.value.copy(desiredSeatCount = count.coerceIn(1, 10), selectedSeatIds = emptySet(), totalPaise = 0) }
    fun selectShowtime(showtimeId: Int) = loadSeatBooking(movieId, showtimeId)
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
        state.value = current.copy(bill = bill, totalPaise = bill.totalPaise)
        pendingCheckoutStore.save(PendingMovieCheckout(
            itemId = movieId, movieTitle = current.movie?.title.orEmpty(), showtimeId = show.id,
            cinemaId = cinema.id, cinemaName = cinema.name, showDatetime = show.showDatetime,
            seatIds = current.selectedSeatIds.sorted(), seatLabels = labels, language = show.language,
            format = show.format, screenName = "Screen ${show.screenId}", seatTier = "Standard",
            venueLocation = listOf(cinema.address, cinema.city).filter(String::isNotBlank).joinToString(", "),
            holdKey = "local-movie-hold", holdExpiresAt = Instant.now().plusSeconds(300).toString(),
            totalPaise = bill.totalPaise, currency = "INR", bill = bill
        ))
        onSuccess()
    }
    fun releaseAndGoBack(onReleased: () -> Unit) { pendingCheckoutStore.clear(); onReleased() }
    fun retry() { if (state.value.showtime == null) loadCinemaOptions(movieId) else loadSeatBooking(movieId, state.value.showtime!!.id) }

    private fun movieDto(id: String): MovieDetailDto? = FakeData.getMovieById(id)?.let { movie -> MovieDetailDto(id.filter(Char::isDigit).toIntOrNull() ?: 1, movie.title, movie.description, listOf(movie.genre), movie.language, rating = movie.rating, releaseDate = movie.releaseDate, status = "NOW_SHOWING") }
    private fun showtime(id: Int, cinemaId: Int, date: LocalDate, hour: Int) = ShowtimeDto(id, 1, cinemaId, 1, "${date}T${hour.toString().padStart(2, '0')}:00:00Z", "${date}T${(hour + 3).coerceAtMost(23).toString().padStart(2, '0')}:00:00Z", "English", if (hour >= 18) "3D" else "2D", 22000, totalSeats = 56, availableSeats = 42, status = "active")
}

internal fun selectMovieSeatBlock(rows: List<Pair<String, MovieSeatDto>>, current: Set<Int>, desiredCount: Int, clickedSeatId: Int): Set<Int> {
    val clickedPair = rows.firstOrNull { it.second.seatId == clickedSeatId } ?: return current
    val clicked = clickedPair.second
    if (clickedSeatId in current) return emptySet()
    if (clicked.status != "available") return current
    val sameRow = rows.filter { it.first == clickedPair.first }.map { it.second }.sortedBy { it.seatNumber }
    val clickedIndex = sameRow.indexOfFirst { it.seatId == clickedSeatId }; if (clickedIndex < 0) return current
    var runStart = clickedIndex
    var runEnd = clickedIndex
    while (runStart > 0 && sameRow[runStart - 1].status == "available" && sameRow[runStart].seatNumber - sameRow[runStart - 1].seatNumber == 1) runStart--
    while (runEnd < sameRow.lastIndex && sameRow[runEnd + 1].status == "available" && sameRow[runEnd + 1].seatNumber - sameRow[runEnd].seatNumber == 1) runEnd++
    if (runEnd - runStart + 1 < desiredCount) return current
    val blockStart = clickedIndex.coerceAtMost(runEnd - desiredCount + 1).coerceAtLeast(runStart)
    return sameRow.subList(blockStart, blockStart + desiredCount).map { it.seatId }.toSet()
}

internal fun previewBill(domain: String, quantity: Int, subtotal: Int): AuthoritativeBillDto {
    val fee = when (domain.uppercase()) {
        "MOVIE" -> 2_000 * quantity
        "EVENT" -> (subtotal * 10) / 100
        "TURF" -> 5_000 + (subtotal * 2) / 100
        else -> 0
    }
    val gst = (fee * 18) / 100
    return AuthoritativeBillDto(domain, quantity, subtotal, taxableAmountPaise = fee,
        cgstPaise = gst / 2, sgstPaise = gst - (gst / 2), gstTotalPaise = gst,
        platformFeePaise = fee, totalPaise = subtotal + fee + gst, currency = "INR",
        calculatedAt = Instant.now().toString())
}
