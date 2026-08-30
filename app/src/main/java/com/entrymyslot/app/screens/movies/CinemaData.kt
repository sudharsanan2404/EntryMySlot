package com.entrymyslot.app.screens.movies

data class Cinema(
    val id: String,
    val name: String,
    val location: String,
    val showTimes: List<String>
)

val sampleCinemas = listOf(
    Cinema(
        id = "1",
        name = "PVR: PVR Heritage",
        location = "Anna Nagar, Chennai",
        showTimes = listOf("10:00 AM", "1:15 PM", "4:30 PM", "7:45 PM", "10:30 PM")
    ),
    Cinema(
        id = "2",
        name = "Sathyam Cinemas",
        location = "Royapettah, Chennai",
        showTimes = listOf("10:30 AM", "2:00 PM", "6:15 PM", "9:30 PM")
    ),
    Cinema(
        id = "3",
        name = "Inox: National",
        location = "Virugambakkam, Chennai",
        showTimes = listOf("11:00 AM", "2:45 PM", "5:30 PM", "8:15 PM", "11:00 PM")
    ),
    Cinema(
        id = "4",
        name = "AGS Cinemas",
        location = "T.Nagar, Chennai",
        showTimes = listOf("10:15 AM", "1:30 PM", "4:45 PM", "8:00 PM")
    )
)
