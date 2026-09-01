package com.entrymyslot.app.screens.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.entrymyslot.app.core.network.NetworkMonitor
import com.entrymyslot.app.data.booking.BookingApi
import com.entrymyslot.app.data.booking.EventZoneDto
import com.entrymyslot.app.data.booking.EventHoldRequest
import com.entrymyslot.app.data.booking.PendingAttendee
import com.entrymyslot.app.data.booking.PendingCheckoutStore
import com.entrymyslot.app.data.booking.PendingEventCheckout
import com.entrymyslot.app.data.details.DetailsApi
import com.entrymyslot.app.data.details.EventDetailDto
import com.entrymyslot.app.data.details.toEventModel
import com.entrymyslot.app.data.model.Event
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class EventBookingOption(
    val id: String,
    val zoneId: Int?,
    val name: String,
    val description: String,
    val pricePaise: Int,
    val remaining: Int,
    val currency: String
)

data class EventBookingUiState(
    val isLoading: Boolean = true,
    val isOffline: Boolean = false,
    val isHolding: Boolean = false,
    val event: Event? = null,
    val rawEvent: EventDetailDto? = null,
    val options: List<EventBookingOption> = emptyList(),
    val selectedOptionId: String? = null,
    val quantity: Int = 0,
    val attendees: List<PendingAttendee> = emptyList(),
    val validationMessage: String? = null,
    val errorMessage: String? = null,
    val httpStatus: Int? = null
    ,val holdKey: String? = null
    ,val holdExpiresAt: String? = null
) {
    val selectedOption: EventBookingOption?
        get() = options.firstOrNull { it.id == selectedOptionId }
    val subtotalPaise: Int
        get() = (selectedOption?.pricePaise ?: 0) * quantity
}

class EventBookingViewModel(
    private val bookingApi: BookingApi,
    private val detailsApi: DetailsApi,
    private val networkMonitor: NetworkMonitor,
    private val pendingCheckoutStore: PendingCheckoutStore
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        EventBookingUiState(isOffline = !networkMonitor.isCurrentlyOnline())
    )
    val uiState: StateFlow<EventBookingUiState> = _uiState.asStateFlow()

    private var eventId: String? = null
    private var loadJob: Job? = null

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

    fun loadEvent(id: String) {
        eventId = id
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
                val (detailResponse, zonesResponse) = coroutineScope {
                    val detail = async { detailsApi.getEventDetails(id) }
                    val zones = async { bookingApi.getEventZones(id) }
                    detail.await() to zones.await()
                }
                val event = detailResponse.data
                val options = zonesResponse.zones
                    .filter(EventZoneDto::is_active)
                    .sortedBy(EventZoneDto::sort_order)
                    .map { zone ->
                        EventBookingOption(
                            id = "zone-${zone.id}",
                            zoneId = zone.id,
                            name = zone.name,
                            description = zone.description.orEmpty(),
                            pricePaise = (zone.price * 100).toInt(),
                            remaining = zone.remaining_capacity,
                            currency = zone.currency
                        )
                    }
                    .ifEmpty { listOf(event.generalAdmissionOption()) }
                _uiState.value = EventBookingUiState(
                    isLoading = false,
                    isOffline = false,
                    event = event.toEventModel(),
                    rawEvent = event,
                    options = options,
                    selectedOptionId = options.firstOrNull()?.id,
                    httpStatus = 200
                )
                recoverCurrentHold(id.toIntOrNull())
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                setError(error)
            }
        }
    }

    fun selectOption(optionId: String) {
        if (_uiState.value.selectedOptionId == optionId) return
        _uiState.value = _uiState.value.copy(
            selectedOptionId = optionId,
            quantity = 0,
            attendees = emptyList(),
            validationMessage = null
        )
    }

    fun setQuantity(quantity: Int) {
        val state = _uiState.value
        val option = state.options.firstOrNull { it.id == state.selectedOptionId } ?: return
        val maxPerBooking = if (state.rawEvent?.isFree == true) 2 else 10
        val safeQuantity = quantity.coerceIn(0, minOf(option.remaining, maxPerBooking))
        val attendees = List(safeQuantity) { index ->
            state.attendees.getOrNull(index) ?: PendingAttendee("", "")
        }
        _uiState.value = state.copy(
            quantity = safeQuantity,
            attendees = attendees,
            validationMessage = null
        )
    }

    fun updateAttendee(index: Int, fullName: String? = null, phone: String? = null) {
        val state = _uiState.value
        if (index !in state.attendees.indices) return
        val updated = state.attendees.toMutableList()
        val current = updated[index]
        updated[index] = current.copy(
            fullName = fullName ?: current.fullName,
            phone = phone ?: current.phone
        )
        _uiState.value = state.copy(attendees = updated, validationMessage = null)
    }

    fun validateSelection(): Boolean {
        val state = _uiState.value
        val event = state.event ?: return false
        val option = state.selectedOption ?: return false
        if (state.quantity == 0) {
            _uiState.value = state.copy(validationMessage = "Select at least one ticket.")
            return false
        }
        val invalidIndex = state.attendees.indexOfFirst { attendee ->
            attendee.fullName.trim().isEmpty() || !PHONE_REGEX.matches(attendee.phone.trim())
        }
        if (invalidIndex >= 0) {
            _uiState.value = state.copy(
                validationMessage = "Enter a valid name and phone for attendee ${invalidIndex + 1}."
            )
            return false
        }
        return true
    }

    fun createHoldAndPrepareCheckout(onSuccess: () -> Unit) {
        if (!validateSelection() || _uiState.value.isHolding) return
        val state = _uiState.value
        val numericEventId = eventId?.toIntOrNull() ?: return
        val option = state.selectedOption ?: return
        viewModelScope.launch {
            _uiState.value = state.copy(isHolding = true, errorMessage = null)
            try {
                val hold = bookingApi.holdEvent(EventHoldRequest(numericEventId, state.quantity, option.zoneId)).data
                saveCheckout(hold)
                onSuccess()
            } catch (error: CancellationException) { throw error }
            catch (error: Throwable) {
                _uiState.value = _uiState.value.copy(isHolding = false)
                setError(error)
            }
        }
    }

    private fun saveCheckout(hold: com.entrymyslot.app.data.booking.EventHoldDto) {
        val state = _uiState.value
        val event = state.event ?: return
        val option = state.selectedOption ?: return
        val key = hold.holdKey ?: return
        val expiresAt = hold.expiresAt ?: return
        val bill = hold.bill ?: return
        _uiState.value = state.copy(isHolding = false, holdKey = key, holdExpiresAt = expiresAt, httpStatus = 201)
        pendingCheckoutStore.save(
            PendingEventCheckout(
                itemId = event.id,
                title = event.title,
                holdKey = key,
                holdExpiresAt = expiresAt,
                zoneId = option.zoneId,
                zoneName = option.name,
                attendees = state.attendees.map { attendee ->
                    PendingAttendee(attendee.fullName.trim(), attendee.phone.trim())
                },
                subtotalPaise = bill.subtotalPaise,
                currency = bill.currency,
                bill = bill
            )
        )
    }

    private fun recoverCurrentHold(numericEventId: Int?) {
        if (numericEventId == null) return
        viewModelScope.launch {
            try {
                val hold = bookingApi.getCurrentEventHold(numericEventId).data
                if (hold.active && hold.quantity > 0) {
                    hold.zoneId?.let { zoneId ->
                        _uiState.value.options.firstOrNull { it.zoneId == zoneId }?.let { option ->
                            _uiState.value = _uiState.value.copy(selectedOptionId = option.id)
                        }
                    }
                    setQuantity(hold.quantity)
                    saveCheckout(hold)
                }
            } catch (_: Throwable) { /* Event selection remains usable. */ }
        }
    }

    fun releaseAndGoBack(onReleased: () -> Unit) {
        val key = _uiState.value.holdKey
        if (key == null) return onReleased()
        viewModelScope.launch {
            runCatching { bookingApi.releaseEventHold(key) }
            pendingCheckoutStore.clear()
            _uiState.value = _uiState.value.copy(holdKey = null, holdExpiresAt = null)
            onReleased()
        }
    }

    fun retry() {
        eventId?.let(::loadEvent)
    }

    private fun setError(error: Throwable) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            isOffline = error.isNetworkFailure() || !networkMonitor.isCurrentlyOnline(),
            errorMessage = when {
                error is HttpException && error.code() == 401 -> "Your session expired. Sign in again."
                error.isNetworkFailure() -> "No internet connection. Reconnect and try again."
                error.message?.isNotBlank() == true -> error.message
                else -> "Unable to load event booking. Try again."
            },
            httpStatus = (error as? HttpException)?.code()
        )
    }

    companion object {
        private val PHONE_REGEX = Regex("^[+]?[\\d\\s\\-()]{7,15}$")
    }
}

private fun EventDetailDto.generalAdmissionOption(): EventBookingOption =
    EventBookingOption(
        id = "general",
        zoneId = null,
        name = "General Admission",
        description = if (isFree) "Free entry" else "Standard event admission",
        pricePaise = (price.toDoubleOrNull()?.times(100))?.toInt() ?: 0,
        remaining = stats?.remaining ?: 0,
        currency = currency
    )

private fun Throwable.isNetworkFailure(): Boolean =
    this is UnknownHostException || this is ConnectException || this is SocketTimeoutException
