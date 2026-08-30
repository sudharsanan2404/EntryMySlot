package com.entrymyslot.app

import android.app.Application
import com.entrymyslot.app.core.network.RetrofitClient

class EntryMySlotApp : Application() {

    companion object {
        lateinit var instance: EntryMySlotApp
            private set
    }

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        RetrofitClient.initialize(this)
        appContainer = AppContainer(this)
    }
}
