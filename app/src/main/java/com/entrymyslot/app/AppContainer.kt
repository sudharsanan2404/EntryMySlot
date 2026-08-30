package com.entrymyslot.app

import android.content.Context
import com.entrymyslot.app.core.network.RetrofitClient
import com.entrymyslot.app.core.storage.AuthTokenStore
import com.entrymyslot.app.data.auth.AuthRepository
import com.entrymyslot.app.data.booking.BookingRepository
import com.entrymyslot.app.data.event.EventRepository
import com.entrymyslot.app.data.movie.MovieRepository
import com.entrymyslot.app.data.turf.TurfRepository

class AppContainer(
    context: Context
) {

    val authTokenStore = AuthTokenStore(
        context = context.applicationContext
    )

    val authRepository = AuthRepository(
        authApi = RetrofitClient.authApi,
        tokenStore = authTokenStore
    )

    val eventRepository = EventRepository(
        eventApi = RetrofitClient.eventApi
    )

    val movieRepository = MovieRepository(
        movieApi = RetrofitClient.movieApi
    )

    val turfRepository = TurfRepository(
        turfApi = RetrofitClient.turfApi
    )

    val bookingRepository = BookingRepository(
        bookingApi = RetrofitClient.bookingApi
    )
}