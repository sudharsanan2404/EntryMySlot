package com.entrymyslot.app

import com.entrymyslot.app.data.FakeData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeDataTest {
    @Test
    fun catalogIdsAreUniqueAndBookingsReferenceExistingItems() {
        val catalog = FakeData.movies + FakeData.turfs + FakeData.events
        assertEquals(catalog.size, catalog.map { it.id }.distinct().size)
        FakeData.bookings.forEach { booking ->
            assertNotNull("Missing item ${booking.itemId}", FakeData.getItemById(booking.itemId))
        }
    }

    @Test
    fun movieShowsAndSeatsReferenceExistingParents() {
        FakeData.movieShows.forEach { show ->
            assertNotNull(FakeData.getMovieById(show.movieId))
            assertNotNull(FakeData.getCinemaById(show.cinemaId))
            assertEquals(56, FakeData.getSeats(show.id).size)
        }
        assertTrue(FakeData.movieSeats.all { seat -> FakeData.movieShows.any { it.id == seat.showId } })
    }

    @Test
    fun everyTurfHasExactlyTwentyFourHourlySlotsForItsAvailableDate() {
        FakeData.turfs.forEach { turf ->
            val slots = FakeData.getSlots(turf.id, turf.availableDate)
            assertEquals(24, slots.size)
            assertEquals((0..23).toList(), slots.map { it.hour }.sorted())
        }
    }

    @Test
    fun everyEventHasTicketTiersAndEveryMovieHasCast() {
        FakeData.events.forEach { event -> assertTrue(FakeData.getTicketTiers(event.id).isNotEmpty()) }
        FakeData.movies.forEach { movie -> assertEquals(movie.castIds.size, FakeData.getCast(movie).size) }
    }
}
