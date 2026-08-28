package com.entrymyslot.app

import android.app.Application
import com.entrymyslot.app.core.network.RetrofitClient

class EntryMySlotApp : Application() {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()

        RetrofitClient.initialize(this)

        appContainer = AppContainer(this)
    }
}