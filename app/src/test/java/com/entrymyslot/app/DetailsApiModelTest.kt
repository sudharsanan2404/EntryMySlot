package com.entrymyslot.app

import com.entrymyslot.app.data.details.EventDetailResponse
import com.entrymyslot.app.data.details.MovieDetailResponse
import com.entrymyslot.app.data.details.TurfDetailResponse
import com.entrymyslot.app.data.details.toEventModel
import com.entrymyslot.app.data.details.toMovieModel
import com.entrymyslot.app.data.details.toTurfModel
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DetailsApiModelTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun movieDetailsResponse_mapsServerFields() {
        val response = json.decodeFromString<MovieDetailResponse>(
            """
            {
              "success": true,
              "data": {
                "id": 12,
                "title": "Avengers",
                "synopsis": "Earth's heroes unite.",
                "genre": ["Action", "Adventure"],
                "language": "English",
                "durationMinutes": 181,
                "cast": ["Robert Downey Jr."],
                "director": "Anthony Russo",
                "rating": 8.4,
                "status": "now_showing"
              }
            }
            """.trimIndent()
        )

        val movie = response.data.toMovieModel()
        assertEquals("12", movie.id)
        assertEquals("3h 1m", movie.duration)
        assertEquals(listOf("Robert Downey Jr."), movie.castNames)
        assertEquals("Anthony Russo", movie.director)
    }

    @Test
    fun eventDetailsResponse_mapsDatePriceAndCapacity() {
        val response = json.decodeFromString<EventDetailResponse>(
            """
            {
              "success": true,
              "data": {
                "id": 10,
                "title": "Music Night",
                "venue": "City Arena",
                "city": "Chennai",
                "event_date": "2026-09-10",
                "start_time": "18:30:00",
                "price": "499.00",
                "currency": "INR",
                "is_free": false,
                "stats": { "capacity": 500, "bookedCount": 125, "remaining": 375 }
              }
            }
            """.trimIndent()
        )

        val event = response.data.toEventModel()
        assertEquals("10", event.id)
        assertTrue(event.date.contains("10 Sep 2026"))
        assertEquals("From ₹499", event.price)
        assertEquals(375, event.remainingCapacity)
    }

    @Test
    fun turfDetailsResponse_acceptsPostgresNumericStrings() {
        val response = json.decodeFromString<TurfDetailResponse>(
            """
            {
              "success": true,
              "data": {
                "id": 1,
                "venue_id": 2,
                "resource_type": "slot_based",
                "category": "Football",
                "name": "Arena Turf",
                "base_price": "1200.00",
                "attributes": {
                  "sports": ["Football"],
                  "images": ["https://example.com/turf.jpg"],
                  "rules": ["Wear turf shoes"],
                  "specifications": { "Size": "5-a-side" }
                },
                "venue_name": "Arena Sports",
                "city": "Chennai",
                "amenities": ["Parking", "Drinking water"],
                "latitude": "13.0827",
                "longitude": "80.2707",
                "avg_rating": "4.50",
                "review_count": "8"
              }
            }
            """.trimIndent()
        )

        val turf = response.data.toTurfModel()
        assertEquals("1", turf.id)
        assertEquals("2", turf.venueId)
        assertEquals(1200, turf.pricePerHour)
        assertEquals(4.5, turf.rating, 0.0)
        assertEquals(8, turf.reviewCount)
        assertEquals(listOf("Football"), turf.sports)
    }
}
