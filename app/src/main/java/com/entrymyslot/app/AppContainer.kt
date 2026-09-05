package com.entrymyslot.app

import android.content.Context
import com.entrymyslot.app.data.booking.PendingCheckoutStore

class AppContainer(
    @Suppress("UNUSED_PARAMETER") context: Context
) {
    val pendingCheckoutStore = PendingCheckoutStore()
}
