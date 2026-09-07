package com.entrymyslot.app

import com.entrymyslot.app.data.booking.AuthoritativeBillDto
import com.entrymyslot.app.data.booking.PendingAttendee
import com.entrymyslot.app.data.booking.PendingCheckoutStore
import com.entrymyslot.app.data.booking.PendingPayment
import com.entrymyslot.app.data.booking.PendingTurfCheckout
import com.entrymyslot.app.data.mapper.bookingStatus
import com.entrymyslot.app.data.mapper.ticketDateTime
import com.entrymyslot.app.data.model.BookingStatus
import com.entrymyslot.app.data.model.TicketDetails
import com.entrymyslot.app.screens.events.EventBookingOption
import com.entrymyslot.app.screens.events.isValidEventAttendee
import com.entrymyslot.app.screens.ticket.TicketUiState
import com.entrymyslot.app.screens.turf.turfPricePaise
import java.time.Instant
import org.junit.Assert.*
import org.junit.Test

class CheckoutRegressionTest {
    @Test fun ticketSwitchKeepsQrSeatAndAttendeeTogether() {
        val first = ticket("qr-one", "A1", "First attendee")
        val second = ticket("qr-two", "A2", "Second attendee")
        val state = TicketUiState(tickets = listOf(first, second)).selectTicket(1)
        assertEquals(1, state.selectedTicketIndex)
        assertEquals("qr-two", state.ticket?.qrPayload)
        assertEquals("A2", state.ticket?.admission)
        assertEquals("Second attendee", state.ticket?.attendee)
        assertEquals(first, state.selectTicket(0).ticket)
    }

    @Test fun ticketSelectionCannotLeaveAvailableBounds() {
        val state = TicketUiState(tickets = listOf(ticket("qr-one", "A1", "Name")))
        assertSame(state, state.selectTicket(-1))
        assertSame(state, state.selectTicket(1))
        assertNull(TicketUiState().selectTicket(0).ticket)
    }

    @Test fun reenteringSameCheckoutPreservesBookingAndSubmissionGuard() {
        val store = PendingCheckoutStore()
        val checkout = turfCheckout()
        store.save(checkout)
        store.markCreationAttempted()
        val payment = PendingPayment("TURF", "1", "booking", "reference", "order", 12550, "https://example.com/pay", null)
        store.savePayment(payment)
        store.save(checkout.copy())
        assertSame(payment, store.payment)
        assertTrue(store.creationAttempted)
        store.save(checkout.copy(unitId = 2))
        assertNull(store.payment)
        assertFalse(store.creationAttempted)
        store.clear()
        assertNull(store.current.value)
    }

    @Test fun pricesRejectInvalidPrecisionOverflowAndNonFiniteValues() {
        assertEquals(12550, turfPricePaise(125.50))
        assertEquals(0, turfPricePaise(0.0))
        assertNull(turfPricePaise(1.001))
        assertNull(turfPricePaise(-1.0))
        assertNull(turfPricePaise(Double.NaN))
        assertNull(turfPricePaise(Double.POSITIVE_INFINITY))
        assertNull(turfPricePaise(30_000_000.0))
    }

    @Test fun ticketQuantitiesRespectStockOrderLimitsAndIntegerTotals() {
        val option = EventBookingOption("one", null, "Ticket", "", 100, 30, "INR", maxPerOrder = 4)
        assertEquals(4, option.maximumQuantity)
        assertEquals(0, option.copy(remaining = -1).maximumQuantity)
        assertEquals(2, option.copy(pricePaise = 1_000_000_000).maximumQuantity)
    }

    @Test fun attendeePhoneRequiresDigitsInsteadOfOnlyCharacterCount() {
        assertTrue(isValidEventAttendee(PendingAttendee("Name", "+91 98765 43210")))
        assertFalse(isValidEventAttendee(PendingAttendee("Name", "abcdefghij")))
        assertFalse(isValidEventAttendee(PendingAttendee("Name", "       ")))
        assertFalse(isValidEventAttendee(PendingAttendee(" ", "9876543210")))
        assertFalse(isValidEventAttendee(PendingAttendee("Name", "12345+6789")))
    }

    @Test fun dateOnlyBookingRemainsUpcomingUntilLocalDayEnds() {
        assertEquals(BookingStatus.UPCOMING, bookingStatus("confirmed", "2026-09-07", Instant.parse("2026-09-07T18:29:00Z")))
        assertEquals(BookingStatus.COMPLETED, bookingStatus("confirmed", "2026-09-07", Instant.parse("2026-09-07T18:31:00Z")))
        assertEquals(BookingStatus.COMPLETED, bookingStatus("confirmed", "2026-09-07 10:00:00", Instant.parse("2026-09-07T12:00:00Z")))
    }

    @Test fun utcTicketDatesRollOverInIndiaAndLocalTimesArePreserved() {
        assertEquals("2026-09-08" to "01:30", ticketDateTime("2026-09-07T20:00:00Z"))
        assertEquals("2026-09-07" to "18:30", ticketDateTime("2026-09-07 18:30:00"))
        assertEquals("" to "", ticketDateTime(null))
    }

    private fun ticket(id: String, seat: String, attendee: String) = TicketDetails(
        bookingId = "booking", ticketUuid = id, title = "Title", category = "MOVIE", venue = "Cinema",
        date = "2026-09-07", time = "18:30", admission = seat, attendee = attendee, amount = "₹250", serverQrPayload = id
    )

    private fun turfCheckout() = PendingTurfCheckout(
        itemId = "1", resourceName = "Turf", unitId = 1, startsAt = "2026-09-07T10:00:00Z", endsAt = "2026-09-07T11:00:00Z",
        formattedTime = "15:30 - 16:30", venueLocation = "Chennai", bookingDate = "2026-09-07", holdToken = "", holdExpiresAt = "",
        subtotalPaise = 12550, currency = "INR", bill = AuthoritativeBillDto("TURF", 1, 12550, currency = "INR")
    )
}
