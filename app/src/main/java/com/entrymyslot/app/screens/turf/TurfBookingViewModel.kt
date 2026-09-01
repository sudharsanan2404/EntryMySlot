package com.entrymyslot.app.screens.turf

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.entrymyslot.app.core.network.NetworkMonitor
import com.entrymyslot.app.data.booking.BookingApi
import com.entrymyslot.app.data.booking.PendingCheckoutStore
import com.entrymyslot.app.data.booking.PendingTurfCheckout
import com.entrymyslot.app.data.booking.TurfHoldRequest
import com.entrymyslot.app.data.booking.TurfSlotDto
import com.entrymyslot.app.data.details.DetailsApi
import com.entrymyslot.app.data.details.toTurfModel
import com.entrymyslot.app.data.model.Turf
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.Instant
import java.time.LocalDate
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

data class TurfBookingUiState(
    val isLoading: Boolean = true,
    val isHolding: Boolean = false,
    val isOffline: Boolean = false,
    val turf: Turf? = null,
    val selectedDate: LocalDate = LocalDate.now(),
    val slots: List<TurfSlotDto> = emptyList(),
    val selectedUnitId: Int? = null,
    val holdToken: String? = null,
    val holdExpiresAt: String? = null,
    val holdSecondsRemaining: Int = 0,
    val errorMessage: String? = null,
    val httpStatus: Int? = null
) {
    val selectedSlot: TurfSlotDto?
        get() = slots.firstOrNull { it.unit_id == selectedUnitId }
}

class TurfBookingViewModel(
    private val bookingApi: BookingApi,
    private val detailsApi: DetailsApi,
    private val networkMonitor: NetworkMonitor,
    private val pendingCheckoutStore: PendingCheckoutStore
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        TurfBookingUiState(isOffline = !networkMonitor.isCurrentlyOnline())
    )
    val uiState: StateFlow<TurfBookingUiState> = _uiState.asStateFlow()

    private var resourceId: String? = null
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

    fun loadTurf(id: String, date: LocalDate = _uiState.value.selectedDate) {
        resourceId = id
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
                val (details, availability) = coroutineScope {
                    val detailsDeferred = async { detailsApi.getTurfDetails(id) }
                    val availabilityDeferred = async { bookingApi.getTurfAvailability(id, date.toString()) }
                    detailsDeferred.await() to availabilityDeferred.await()
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isOffline = false,
                    turf = details.data.toTurfModel(),
                    slots = availability.data.slots,
                    errorMessage = null,
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

    fun changeDate(date: LocalDate) {
        if (_uiState.value.holdToken != null) return
        resourceId?.let { loadTurf(it, date) }
    }

    fun onSlotClicked(slot: TurfSlotDto) {
        val state = _uiState.value
        if (state.isHolding || state.holdToken != null) return
        if (slot.unit_id == state.selectedUnitId) {
            _uiState.value = state.copy(selectedUnitId = null, errorMessage = null)
            return
        }
        if (slot.status != "available") return

        _uiState.value = state.copy(selectedUnitId = slot.unit_id, errorMessage = null)
    }

    fun validateSelection(): Boolean {
        if (_uiState.value.selectedSlot != null) return true
        _uiState.value = _uiState.value.copy(errorMessage = "Select a slot before continuing.")
        return false
    }

    fun createHoldAndPrepareCheckout(onSuccess: () -> Unit) {
        if (!validateSelection() || _uiState.value.isHolding) return
        val slot = _uiState.value.selectedSlot ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isHolding = true, errorMessage = null)
            try {
                val hold = bookingApi.holdTurfSlot(TurfHoldRequest(slot.unit_id)).data
                applyHoldAndSave(hold.token, hold.expiresAt, hold.unitId, hold.bill)
                onSuccess()
            } catch (error: CancellationException) { throw error }
            catch (error: Throwable) {
                _uiState.value = _uiState.value.copy(isHolding = false)
                setError(error)
                refreshAvailability()
            }
        }
    }

    private fun applyHoldAndSave(token: String, expiresAt: String, unitId: Int, bill: com.entrymyslot.app.data.booking.AuthoritativeBillDto) {
        val state = _uiState.value
        val turf = state.turf ?: return
        val slot = state.slots.firstOrNull { it.unit_id == unitId } ?: return
        _uiState.value = state.copy(isHolding = false, selectedUnitId = unitId, holdToken = token,
            holdExpiresAt = expiresAt, holdSecondsRemaining = secondsUntil(expiresAt), errorMessage = null, httpStatus = 201)
        pendingCheckoutStore.save(
            PendingTurfCheckout(
                itemId = turf.id,
                resourceName = turf.title,
                unitId = slot.unit_id,
                startsAt = slot.starts_at,
                endsAt = slot.ends_at,
                formattedTime = slot.formatted_time,
                holdToken = token,
                holdExpiresAt = expiresAt,
                subtotalPaise = bill.subtotalPaise,
                currency = bill.currency,
                bill = bill
            )
        )
        startCountdown()
        refreshAvailability()
    }

    fun releaseAndGoBack(onReleased: () -> Unit) {
        val token = _uiState.value.holdToken
        if (token == null) {
            onReleased()
            return
        }
        viewModelScope.launch {
            runCatching { bookingApi.releaseTurfHold(token) }
            clearHoldState()
            onReleased()
        }
    }

    fun retry() {
        resourceId?.let { loadTurf(it, _uiState.value.selectedDate) }
    }

    private fun releaseHold(clearSelection: Boolean) {
        val token = _uiState.value.holdToken
        if (clearSelection) clearHoldState()
        if (token != null) viewModelScope.launch { runCatching { bookingApi.releaseTurfHold(token) } }
    }

    private fun clearHoldState() {
        countdownJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isHolding = false,
            selectedUnitId = null,
            holdToken = null,
            holdExpiresAt = null,
            holdSecondsRemaining = 0
        )
    }

    private fun recoverCurrentHold(numericResourceId: Int?) {
        if (numericResourceId == null) return
        viewModelScope.launch {
            try {
                val current = bookingApi.getCurrentTurfHold(numericResourceId).data
                if (current.active && current.token != null && current.expiresAt != null && current.unitId != null && current.bill != null) {
                    applyHoldAndSave(current.token, current.expiresAt, current.unitId, current.bill)
                }
            } catch (_: Throwable) { /* Availability remains usable if recovery is unavailable. */ }
        }
    }

    private fun refreshAvailability() {
        val id = resourceId ?: return
        val date = _uiState.value.selectedDate
        viewModelScope.launch {
            try {
                val response = bookingApi.getTurfAvailability(id, date.toString())
                _uiState.value = _uiState.value.copy(slots = response.data.slots, isOffline = false)
            } catch (_: Throwable) {
                // Preserve visible data during a background refresh failure.
            }
        }
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (true) {
                val expiresAt = _uiState.value.holdExpiresAt ?: break
                val remaining = secondsUntil(expiresAt)
                _uiState.value = _uiState.value.copy(holdSecondsRemaining = remaining)
                if (remaining == 0) {
                    clearHoldState()
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Your five-minute slot hold expired. Select the slot again."
                    )
                    refreshAvailability()
                    break
                }
                delay(1_000)
            }
        }
    }

    private fun setError(error: Throwable) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            isHolding = false,
            isOffline = error.isNetworkFailure() || !networkMonitor.isCurrentlyOnline(),
            errorMessage = when {
                error is HttpException && error.code() == 401 -> "Your session expired. Sign in again."
                error is HttpException && error.code() == 409 -> "That slot was just reserved by another user. Choose another slot."
                error.isNetworkFailure() -> "No internet connection. Reconnect and try again."
                error.message?.isNotBlank() == true -> error.message
                else -> "Unable to load Turf availability. Try again."
            },
            httpStatus = (error as? HttpException)?.code()
        )
    }
}

private fun secondsUntil(expiresAt: String): Int = runCatching {
    ((Instant.parse(expiresAt).toEpochMilli() - System.currentTimeMillis() + 999) / 1000).toInt()
}.getOrDefault(0).coerceAtLeast(0)

private fun Throwable.isNetworkFailure(): Boolean =
    this is UnknownHostException || this is ConnectException || this is SocketTimeoutException
