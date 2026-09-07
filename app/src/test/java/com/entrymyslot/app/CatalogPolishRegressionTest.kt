package com.entrymyslot.app

import com.entrymyslot.app.data.mapper.toUi
import com.entrymyslot.app.screens.search.PriceFilter
import com.entrymyslot.app.screens.search.SearchResult
import com.entrymyslot.app.screens.search.SearchResultType
import com.entrymyslot.app.screens.search.SearchSort
import com.entrymyslot.app.screens.search.SearchUiState
import com.entrymyslot.app.screens.search.filterSearchResults
import com.entrymyslot.app.screens.search.searchPrice
import com.entrymyslotbe.app.network.model.Event
import com.entrymyslotbe.app.network.model.TicketTier
import com.entrymyslotbe.app.network.model.Zone
import java.math.BigDecimal
import org.junit.Assert.*
import org.junit.Test

class CatalogPolishRegressionTest {
    @Test fun zonePricesKeepPrecisionAndDoNotOverflowIntegerPaise() {
        assertEquals("₹100.005", Event(id = "e", zones = listOf(Zone(price = BigDecimal("100.005")))).toUi()!!.price)
        assertEquals("₹30000000.00", Event(id = "e", zones = listOf(Zone(price = BigDecimal("30000000.00")))).toUi()!!.price)
    }

    @Test fun ticketTierPricesStillConvertPaiseToRupees() {
        assertEquals("₹125.50", Event(id = "e", ticketTiers = listOf(TicketTier(price = 12550))).toUi()!!.price)
    }

    @Test fun eventDateAndTimeUseTheSameIndiaTimezoneAtMidnight() {
        val event = Event(id = "e", startDate = "2026-09-30T20:00:00Z").toUi()!!
        assertEquals("2026-10-01", event.date)
        assertEquals("01:30", event.time)
    }

    @Test fun plainEventDatesRemainUnchanged() {
        assertEquals("2026-09-30", Event(id = "e", startDate = "2026-09-30").toUi()!!.date)
    }

    @Test fun blankLocationPartsDoNotLeaveTrailingCommas() {
        assertEquals("Chennai", Event(id = "e", venue = "", city = "Chennai").toUi()!!.location)
    }

    @Test fun priceRangesUseTheirStartingPriceAndFreeHasZeroValue() {
        assertEquals(500.0, searchPrice("₹500 - ₹1,500")!!, 0.0)
        assertEquals(1250.5, searchPrice("From ₹1,250.50 per hour")!!, 0.0)
        assertEquals(0.0, searchPrice("Free")!!, 0.0)
        assertNull(searchPrice("Price unavailable"))
    }

    @Test fun unknownPricesSortLastInBothDirections() {
        val results = listOf(result("unknown", ""), result("high", "₹750"), result("free", "Free"), result("low", "₹250"))
        assertEquals(listOf("free", "low", "high", "unknown"), filterSearchResults(results, SearchUiState(sort = SearchSort.PRICE_LOW)).map { it.item.id })
        assertEquals(listOf("high", "low", "free", "unknown"), filterSearchResults(results, SearchUiState(sort = SearchSort.PRICE_HIGH)).map { it.item.id })
    }

    @Test fun fiveHundredBelongsOnlyInFiveHundredPlusFilter() {
        val results = listOf(result("low", "₹499.99"), result("boundary", "₹500"), result("free", "Free"), result("unknown", ""))
        assertEquals(listOf("low"), filterSearchResults(results, SearchUiState(priceFilter = PriceFilter.UNDER_500)).map { it.item.id })
        assertEquals(listOf("boundary"), filterSearchResults(results, SearchUiState(priceFilter = PriceFilter.ABOVE_500)).map { it.item.id })
        assertEquals(listOf("free"), filterSearchResults(results, SearchUiState(priceFilter = PriceFilter.FREE)).map { it.item.id })
    }

    @Test fun queryTrimsWhitespaceAndHonorsSelectedTypes() {
        val event = result("Live music", "₹250")
        assertEquals(listOf(event), filterSearchResults(listOf(event), SearchUiState(query = "  MUSIC  ")))
        assertTrue(filterSearchResults(listOf(event), SearchUiState(selectedTypes = setOf(SearchResultType.MOVIE))).isEmpty())
    }

    private fun result(id: String, price: String): SearchResult = SearchResult(
        com.entrymyslot.app.data.model.Event(id, id, "", "", price, description = "", category = "", time = ""),
        SearchResultType.EVENT
    )
}
