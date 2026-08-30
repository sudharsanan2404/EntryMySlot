package com.entrymyslot.app.data.model

/** A compact presentation model shared by each discovery row on the home screen. */
data class HomeContent(
    val id: String,
    val title: String,
    val date: String,
    val location: String,
    val price: String,
    val imageUrl: String? = null
)