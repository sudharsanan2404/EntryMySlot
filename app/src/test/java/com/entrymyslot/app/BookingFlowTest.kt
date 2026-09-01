package com.entrymyslot.app

import com.entrymyslot.app.data.booking.MovieSeatDto
import com.entrymyslot.app.screens.movies.selectMovieSeatBlock
import org.junit.Assert.assertEquals
import org.junit.Test

class BookingFlowTest {
    @Test
    fun isolatedTwoSeatBlockCanBeCompletedWithThreeSeatsInSameTierRow() {
        val rows = listOf(
            "A" to seat(1, 1),
            "A" to seat(2, 2),
            "A" to seat(3, 3, status = "booked"),
            "B" to seat(4, 1),
            "B" to seat(5, 2),
            "B" to seat(6, 3),
            "B" to seat(7, 4)
        )

        val firstClick = selectMovieSeatBlock(rows, emptySet(), 5, clickedSeatId = 1)
        assertEquals(setOf(1, 2), firstClick)

        val secondClick = selectMovieSeatBlock(rows, firstClick, 5, clickedSeatId = 4)
        assertEquals(setOf(1, 2, 4, 5, 6), secondClick)
    }

    @Test
    fun selectionCannotCrossServerTier() {
        val rows = listOf(
            "A" to seat(1, 1, type = "standard"),
            "A" to seat(2, 2, type = "standard"),
            "C" to seat(8, 1, type = "premium"),
            "C" to seat(9, 2, type = "premium")
        )

        val regular = selectMovieSeatBlock(rows, emptySet(), 3, clickedSeatId = 1)
        val attemptedPremium = selectMovieSeatBlock(rows, regular, 3, clickedSeatId = 8)

        assertEquals(setOf(1, 2), regular)
        assertEquals(regular, attemptedPremium)
    }

    private fun seat(
        id: Int,
        number: Int,
        status: String = "available",
        type: String = "standard"
    ) = MovieSeatDto(
        seatId = id,
        seatNumber = number,
        seatType = type,
        seatCategory = "regular",
        status = status,
        pricePaise = if (type == "premium") 25000 else 18000
    )
}
