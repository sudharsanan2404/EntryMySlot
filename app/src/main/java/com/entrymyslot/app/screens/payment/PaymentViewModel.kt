package com.entrymyslot.app.screens.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.entrymyslot.app.data.booking.PendingCheckoutStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class ConfirmedBookingRoute(val type: String, val itemId: String, val bookingKey: String, val ticketUuid: String)
data class PaymentUiState(val isProcessing: Boolean = false, val errorMessage: String? = null, val confirmedBooking: ConfirmedBookingRoute? = null)

class PaymentViewModel(private val pendingCheckoutStore: PendingCheckoutStore) : ViewModel() {
    private val state = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = state.asStateFlow()
    fun completeFakePayment() {
        val checkout = pendingCheckoutStore.current.value ?: return run { state.value = PaymentUiState(errorMessage = "Booking selection expired. Please select again.") }
        if (state.value.isProcessing) return
        viewModelScope.launch {
            state.value = PaymentUiState(isProcessing = true)
            delay(900)
            val type = checkout::class.simpleName.orEmpty().removePrefix("Pending").removeSuffix("Checkout").uppercase()
            val reference = "EMS-${(System.currentTimeMillis() % 1000000).toString().padStart(6, '0')}"
            state.value = PaymentUiState(confirmedBooking = ConfirmedBookingRoute(type, checkout.itemId, reference, UUID.randomUUID().toString()))
        }
    }
    fun clearError() { state.value = state.value.copy(errorMessage = null) }
}
