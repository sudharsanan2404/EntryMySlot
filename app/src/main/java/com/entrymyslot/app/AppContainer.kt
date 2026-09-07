package com.entrymyslot.app

import android.content.Context
import com.entrymyslot.app.data.booking.PendingCheckoutStore
import com.entrymyslotbe.app.EntryMySlotBackend

class AppContainer(
    private val context: Context
) {
    val backend = EntryMySlotBackend.get(context)
    val selectedCity: String get() = context.getSharedPreferences("entry_my_slot_preferences", 0).getString("selected_city", "").orEmpty()
    val pendingCheckoutStore = PendingCheckoutStore()
}
