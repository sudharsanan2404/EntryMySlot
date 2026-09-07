package com.entrymyslot.app

import com.entrymyslot.app.data.mapper.*
import com.entrymyslot.app.data.model.BookingStatus
import com.entrymyslotbe.app.network.model.*
import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

class NetworkMappingTest {
    private val gson = Gson()

    @Test fun movieFieldsMatchObservedServerNames() {
        // Field names and values observed from the public API; not an app data source.
        val movie = gson.fromJson("""{"id":12,"title":"Avengers: Endgame","synopsis":"Earth's heroes assemble for a final battle to restore the universe.","durationMinutes":181,"rating":8.4}""", Movie::class.java)
        assertEquals("12", movie.toUi()?.id)
        assertEquals(181, movie.duration)
        assertFalse(movie.toUi()!!.description.isBlank())
        assertEquals("", movie.toUi()!!.price)
    }

    @Test fun seatRowsAndPriceAliasesMatchServer() {
        val response = gson.fromJson("""{"showtimeId":14,"rows":[{"rowLabel":"A","seats":[{"seatId":6,"seatNumber":1,"seatType":"standard","seatCategory":"regular","status":"available","pricePaise":18000}]}]}""", SeatsResponse::class.java)
        assertEquals("6", response.rows!!.single().seats.single().id)
        assertEquals(18000, response.rows.single().seats.single().price)
        val price = gson.fromJson("""{"seatId":6,"basePricePaise":18000,"finalPricePaise":18000}""", PriceBreakdown::class.java)
        assertEquals(18000, price.finalPrice)
        val payload = gson.toJson(HoldSeatsRequest(listOf("6"), "14"))
        assertTrue(payload.contains("\"showtimeId\":\"14\""))
    }

    @Test fun venueAndResourceIdsStayDistinct() {
        val resource = gson.fromJson("""{"id":1,"venue_id":2,"base_price":"800.00","category":"Football","resource_type":"slot_based","amenities":["Football"]}""", Venue::class.java)
        val ui = resource.toUi()!!
        assertEquals("1", ui.id)
        assertEquals("2", ui.venueId)
        assertEquals(800, ui.pricePerHour)
        assertEquals(listOf("Football"), ui.facilities)
    }

    @Test fun missingEntitiesDoNotBecomeInventedUiItems() {
        assertNull(Movie().toUi())
        assertNull(Event().toUi())
        assertNull(Venue().toUi())
        assertNull(User().toUi())
        assertNull(Booking().toUiBooking())
    }

    @Test fun pendingReservationDoesNotBecomeConfirmed() {
        assertEquals(BookingStatus.PENDING, bookingStatus("pending_payment", null))
        assertEquals(BookingStatus.CANCELLED, bookingStatus("failed", null))
        assertNull(bookingStatus("unrecognized", null))
    }

    @Test fun holdStatusUsesActiveAndExpiryFields() {
        val hold = gson.fromJson("""{"active":false,"ttlSeconds":0,"seatIds":[],"expiresAt":null}""", HoldSeatsResponse::class.java)
        assertEquals(false, hold.active)
        assertFalse(hold.success)
        assertNull(hold.holdExpiresAt)
    }

    @Test fun eventTimesUseServerWallTimesOrConvertUtcToIndiaTime() {
        val event = gson.fromJson("""{"id":"8","start_at":"2026-09-30T13:00:00.000Z","end_at":"2026-09-30T17:00:00.000Z","start_time":"18:30:00","end_time":"22:30:00"}""", Event::class.java)
        assertEquals("18:30", event.toUi()!!.time)
        assertEquals("22:30", event.toUi()!!.endTime)
        assertEquals("18:30", event.copy(startTime = "").toUi()!!.time)
        assertEquals("22:30", event.copy(endTime = null).toUi()!!.endTime)
    }
}
