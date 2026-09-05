package com.entrymyslot.app.data

import androidx.compose.runtime.mutableStateListOf
import com.entrymyslot.app.data.model.AppNotification
import com.entrymyslot.app.data.model.Booking
import com.entrymyslot.app.data.model.BookingDetails
import com.entrymyslot.app.data.model.BookingStatus
import com.entrymyslot.app.data.model.BookingType
import com.entrymyslot.app.data.model.CastMember
import com.entrymyslot.app.data.model.CatalogItem
import com.entrymyslot.app.data.model.Cinema
import com.entrymyslot.app.data.model.Event
import com.entrymyslot.app.data.model.HomePromotion
import com.entrymyslot.app.data.model.Movie
import com.entrymyslot.app.data.model.MovieSeat
import com.entrymyslot.app.data.model.MovieShow
import com.entrymyslot.app.data.model.NotificationKind
import com.entrymyslot.app.data.model.PaymentMethod
import com.entrymyslot.app.data.model.PaymentMethodType
import com.entrymyslot.app.data.model.PromotionDestination
import com.entrymyslot.app.data.model.TicketDetails
import com.entrymyslot.app.data.model.TicketTier
import com.entrymyslot.app.data.model.Turf
import com.entrymyslot.app.data.model.TurfSlot
import com.entrymyslot.app.data.model.UserProfile

/** Static preview content for the navigation-only frontend. */
object FakeData {
    var currentUser = UserProfile(
        id = "user_001",
        fullName = "Navaneethan",
        email = "navaneethan@email.com",
        phone = "+91 98765 43210",
        city = "",
        memberSince = "August 2026"
    )

    val castMembers = listOf(
        CastMember("cast_001", "Lead Actor", "Actor", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=240&h=240&fit=crop"),
        CastMember("cast_002", "Lead Actress", "Actor", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=240&h=240&fit=crop"),
        CastMember("cast_003", "Director", "Director", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=240&h=240&fit=crop"),
        CastMember("cast_004", "Producer", "Producer", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=240&h=240&fit=crop")
    )

    private val defaultCastIds = castMembers.map { it.id }
    val movies = listOf(
        Movie("mov_1", "The Dark Knight", "In Cinemas Now", "IMAX, Chennai", "From ₹190", description = "A masked vigilante faces a criminal mastermind who pushes Gotham into chaos.", rating = 8.5, language = "Tamil", genre = "Action", duration = "2h 35m", releaseDate = "28 Aug 2026", castIds = defaultCastIds),
        Movie("mov_2", "Inception", "Re-releasing Soon", "PVR, Bangalore", "From ₹250", description = "A skilled extractor enters layered dreams for one last impossible mission.", rating = 8.8, language = "English", genre = "Sci-Fi", duration = "2h 28m", releaseDate = "04 Sep 2026", castIds = defaultCastIds),
        Movie("mov_3", "Interstellar", "15 Oct 2026", "Luxe, Mumbai", "From ₹300", description = "Explorers travel through a wormhole to find humanity a new home.", rating = 8.7, language = "English", genre = "Sci-Fi", duration = "2h 49m", releaseDate = "15 Oct 2026", castIds = defaultCastIds),
        Movie("mov_4", "Avatar: Way of Water", "In Cinemas Now", "PVR, Chennai", "From ₹220", description = "A family fights to protect Pandora and the people who call it home.", rating = 7.6, language = "English", genre = "Adventure", duration = "3h 12m", releaseDate = "20 Aug 2026", castIds = defaultCastIds),
        Movie("mov_5", "The Matrix", "Next Week", "Sathyam, Chennai", "From ₹180", description = "A hacker discovers that his world is an elaborate simulation.", rating = 8.7, language = "English", genre = "Action", duration = "2h 16m", releaseDate = "07 Sep 2026", castIds = defaultCastIds),
        Movie("mov_6", "Avengers: Endgame", "20 Oct 2026", "INOX, Madurai", "From ₹150", description = "Earth's heroes assemble for a final battle to restore the universe.", rating = 8.4, language = "English", genre = "Action", duration = "3h 1m", releaseDate = "20 Oct 2026", castIds = defaultCastIds)
    )

    val cinemas = listOf(
        Cinema("cinema_001", "PVR Cinemas", "Phoenix Marketcity, Chennai"),
        Cinema("cinema_002", "INOX", "VR Mall, Chennai"),
        Cinema("cinema_003", "AGS Cinemas", "T. Nagar, Chennai"),
        Cinema("cinema_004", "Sathyam Cinemas", "Royapettah, Chennai")
    )

    private val cinemaTimes = listOf(
        listOf("10:30 AM", "01:45 PM", "04:30 PM", "07:15 PM", "10:30 PM"),
        listOf("11:00 AM", "02:30 PM", "05:00 PM", "08:15 PM", "11:00 PM"),
        listOf("10:00 AM", "01:00 PM", "04:00 PM", "07:00 PM", "10:00 PM"),
        listOf("10:45 AM", "02:00 PM", "05:15 PM", "08:30 PM", "11:15 PM")
    )
    val movieShows = cinemas.flatMapIndexed { cinemaIndex, cinema ->
        movies.flatMap { movie ->
            cinemaTimes[cinemaIndex].mapIndexed { timeIndex, time ->
                MovieShow("show_${movie.id}_${cinema.id}_$timeIndex", movie.id, cinema.id, "2026-08-31", time)
            }
        }
    }
    private val bookedSeatLabels = setOf("A2", "B4", "C1", "D3", "E2", "F4", "A6", "B7", "C5", "E8")
    val movieSeats = movieShows.flatMap { show ->
        ('A'..'G').flatMap { row ->
            (1..8).map { number ->
                val label = "$row$number"
                MovieSeat(show.id, label, label in bookedSeatLabels)
            }
        }
    }

    val turfs = listOf(
        Turf("sport_1", "Green Arena Turf", "Open Now", "Adyar, Chennai", "From ₹800", description = "Premium 5-a-side football turf with professional artificial grass, floodlights and comfortable facilities. Perfect for casual games and tournaments.", rating = 4.8, venueType = "5-a-side Turf", sports = listOf("Football", "Cricket"), facilities = listOf("Football", "Floodlights", "Changing Room", "Parking"), pricePerHour = 800),
        Turf("sport_2", "Blue Wave Pool", "6:00 AM - 9:00 PM", "Velachery, Chennai", "From ₹200", description = "Pristine Olympic-sized swimming pool with temperature control and dedicated lanes for training and recreation.", rating = 4.6, venueType = "Aquatic Centre", sports = listOf("Swimming", "Diving"), facilities = listOf("Swimming", "Changing Room", "Parking"), pricePerHour = 200),
        Turf("sport_3", "Elite Badminton Club", "Available Today", "T. Nagar, Chennai", "From ₹400", description = "State-of-the-art indoor badminton facility with professional wooden courts, synthetic mats and LED lighting.", rating = 4.7, venueType = "Indoor Court", sports = listOf("Badminton", "Table Tennis"), facilities = listOf("Badminton", "Floodlights", "Changing Room", "Parking"), pricePerHour = 400),
        Turf("sport_4", "Victory Cricket Ground", "Slots Available", "OMR, Chennai", "From ₹1500", description = "Full-size cricket ground suitable for league matches and team practice.", rating = 4.5, venueType = "Cricket Ground", sports = listOf("Cricket"), facilities = listOf("Cricket", "Floodlights", "Changing Room", "Parking"), pricePerHour = 1500),
        Turf("sport_5", "Smash Tennis Court", "Open 24/7", "Anna Nagar, Chennai", "From ₹600", description = "Well-maintained tennis courts with coaching and equipment rental.", rating = 4.4, venueType = "Tennis Court", sports = listOf("Tennis"), facilities = listOf("Tennis", "Floodlights", "Parking"), pricePerHour = 600),
        Turf("sport_6", "Dunk Basket Court", "Available Now", "Porur, Chennai", "From ₹300", description = "Outdoor basketball court with quality flooring and night lighting.", rating = 4.3, venueType = "Basketball Court", sports = listOf("Basketball"), facilities = listOf("Basketball", "Floodlights", "Parking"), pricePerHour = 300)
    )
    val turfSlots = turfs.flatMap { turf ->
        (0..23).map { hour ->
            TurfSlot(turf.id, turf.availableDate, hour, hourLabel(hour), hour in setOf(2, 4, 7, 10, 13, 17, 20))
        }
    }

    val events = listOf(
        Event("event_001", "Live Cricket Championship", "30 Aug 2026 | 6:30 PM", "Nehru Stadium, Chennai", "From ₹400", description = "An electric evening of live cricket with premium stadium experiences.", category = "Sports", time = "6:30 PM"),
        Event("event_002", "Arijit Singh Live", "25 Nov 2026 | 7:00 PM", "DY Patil Stadium, Mumbai", "From ₹799", description = "An unforgettable live concert featuring chart-topping favourites.", category = "Concert", time = "7:00 PM"),
        Event("event_003", "Live Music Night", "12 Dec 2026 | 8:00 PM", "Chennai", "From ₹499", description = "A curated night of independent music and local performers.", category = "Concert", time = "8:00 PM"),
        Event("event_004", "Tech Summit 2026", "15 Jan 2027 | 10:00 AM", "Trade Center, Bangalore", "Free Entry", description = "Technology leaders discuss products, startups and the future of AI.", category = "Conference", time = "10:00 AM"),
        Event("event_005", "Stand-up Comedy", "05 Dec 2026 | 9:00 PM", "The Laugh Club, Chennai", "From ₹350", description = "A sharp new stand-up set from popular comedians.", category = "Comedy", time = "9:00 PM"),
        Event("event_006", "Food Festival", "10 Nov 2026 | 11:00 AM", "Island Ground, Chennai", "From ₹100", description = "Street food, regional favourites and live entertainment.", category = "Festival", time = "11:00 AM")
    )
    val ticketTiers = events.flatMap { event ->
        listOf(
            TicketTier("${event.id}_vip", event.id, "VIP", 2500, "Premium seating with best venue view", 18),
            TicketTier("${event.id}_platinum", event.id, "Platinum", 1800, "Excellent view from elevated platform", 45),
            TicketTier("${event.id}_gold", event.id, "Gold", 1200, "Good view from center stands", 120),
            TicketTier("${event.id}_silver", event.id, "Silver", 700, "Standard view from side stands", 200),
            TicketTier("${event.id}_general", event.id, "General", 400, "Entry level seating", 0, true)
        )
    }

    val bookings = listOf(
        Booking("booking_001", currentUser.id, BookingType.MOVIE, "mov_1", "cinema_001", "28 Aug 2026 • 1:30 PM", "Seats: A3, A4", "₹360", BookingStatus.UPCOMING),
        Booking("booking_002", currentUser.id, BookingType.TURF, "sport_1", dateTime = "29 Aug 2026 • 6:00 PM - 8:00 PM", details = "2 Hours Booked", price = "₹1,600", status = BookingStatus.UPCOMING),
        Booking("booking_003", currentUser.id, BookingType.EVENT, "event_001", dateTime = "30 Aug 2026 • 6:30 PM", details = "VIP × 2 • Gold × 2", price = "₹7,400", status = BookingStatus.UPCOMING),
        Booking("booking_004", currentUser.id, BookingType.MOVIE, "mov_1", "cinema_004", "25 Aug 2026 • 7:30 PM", "Seats: B4, B5", "₹360", BookingStatus.COMPLETED),
        Booking("booking_005", currentUser.id, BookingType.TURF, "sport_1", dateTime = "20 Aug 2026 • 5:00 PM", details = "1 Hour Booked", price = "₹800", status = BookingStatus.CANCELLED)
    )

    val notifications = mutableStateListOf(
        AppNotification(1, "Venue reminder · Today, 6:00 PM", "Green Arena Turf is booked for 2 hours. Arrive 15 minutes early.", NotificationKind.REMINDER),
        AppNotification(2, "Booking confirmed", "Your Live Cricket Championship tickets are ready in My Bookings.", NotificationKind.BOOKING),
        AppNotification(3, "Weekend venue offer", "Save 20% on selected badminton courts near Chennai this weekend.", NotificationKind.OFFER)
    )

    val promotions = listOf(
        HomePromotion("promo_movies", "NOW SHOWING", "Big-screen stories await", "Find a showtime and reserve your perfect seats.", "Explore movies", PromotionDestination.MOVIES),
        HomePromotion("promo_sports", "SPORTS NEAR YOU", "Own the next game", "Discover nearby venues and book your slot.", "Find a venue", PromotionDestination.SPORTS),
        HomePromotion("promo_events", "LIVE EXPERIENCES", "Make tonight memorable", "Browse events worth stepping out for.", "View events", PromotionDestination.EVENTS)
    )

    val paymentMethods = listOf(
        PaymentMethod("upi", PaymentMethodType.UPI, "UPI (GPay, PhonePe, Paytm)", "Pay instantly using any UPI app", "FASTEST"),
        PaymentMethod("card", PaymentMethodType.CARD, "Credit / Debit Card", "Visa, Mastercard, RuPay"),
        PaymentMethod("netbanking", PaymentMethodType.NET_BANKING, "Netbanking", "All major Indian banks supported")
    )
    val confirmedTicket = TicketDetails(
        bookingId = "EMS-260830",
        title = "Booking Confirmed",
        category = "Entry Pass",
        venue = "EntryMySlot Venue",
        date = "30 Aug 2026",
        time = "Confirmed",
        admission = "1 Guest",
        attendee = currentUser.fullName,
        amount = "₹408"
    )
    val cities = listOf(
        "Ariyalur", "Chengalpattu", "Chennai", "Coimbatore", "Cuddalore",
        "Dharmapuri", "Dindigul", "Erode", "Kallakurichi", "Kancheepuram",
        "Kanniyakumari", "Karur", "Krishnagiri", "Madurai", "Mayiladuthurai",
        "Nagapattinam", "Namakkal", "Nilgiris", "Perambalur", "Pudukkottai",
        "Ramanathapuram", "Ranipet", "Salem", "Sivaganga", "Tenkasi",
        "Thanjavur", "Theni", "Thoothukudi", "Tiruchirappalli", "Tirunelveli",
        "Tirupathur", "Tiruppur", "Tiruvallur", "Tiruvannamalai", "Tiruvarur",
        "Vellore", "Viluppuram", "Virudhunagar"
    )

    val upcomingBookings: List<Booking> get() = bookings.filter { it.status == BookingStatus.UPCOMING }
    val pastBookings: List<Booking> get() = bookings.filter { it.status != BookingStatus.UPCOMING }
    fun getMovieById(id: String): Movie? = movies.find { it.id == id }
    fun getTurfById(id: String): Turf? = turfs.find { it.id == id }
    fun getEventById(id: String): Event? = events.find { it.id == id }
    fun getCinemaById(id: String): Cinema? = cinemas.find { it.id == id }
    fun getItemById(id: String): CatalogItem? = getMovieById(id) ?: getTurfById(id) ?: getEventById(id)
    fun getBookingsForUser(userId: String): List<Booking> = bookings.filter { it.userId == userId }
    fun getShows(movieId: String, cinemaId: String): List<MovieShow> = movieShows.filter { it.movieId == movieId && it.cinemaId == cinemaId }
    fun getSeats(showId: String): List<MovieSeat> = movieSeats.filter { it.showId == showId }
    fun getSlots(turfId: String, date: String? = null): List<TurfSlot> = turfSlots.filter {
        it.turfId == turfId && (date == null || it.date == date)
    }
    fun getTicketTiers(eventId: String): List<TicketTier> = ticketTiers.filter { it.eventId == eventId }
    fun getCast(movie: Movie): List<CastMember> = movie.castIds.mapNotNull { id -> castMembers.find { it.id == id } }
    fun getCinemaShowTimes(cinemaId: String, movieId: String = movies.first().id): List<String> = getShows(movieId, cinemaId).map { it.time }
    fun getBookingVenue(booking: Booking): String = booking.venueId?.let(::getCinemaById)?.name ?: getItemById(booking.itemId)?.location.orEmpty()
    fun createBookingDetails(itemId: String, type: BookingType): BookingDetails {
        val item = getItemById(itemId)
        val amount = when (type) {
            BookingType.MOVIE -> getMovieById(itemId)?.ticketPrice ?: 180
            BookingType.TURF -> getTurfById(itemId)?.pricePerHour ?: 800
            BookingType.EVENT -> getTicketTiers(itemId).firstOrNull { !it.isSoldOut }?.price ?: 400
        }
        return BookingDetails(
            itemId = itemId,
            title = item?.title ?: "Booking",
            category = type,
            date = "28 Aug 2026",
            time = "1:30 PM",
            location = item?.location ?: currentUser.city,
            details = when (type) {
                BookingType.MOVIE -> "Seats: A3, A4"
                BookingType.TURF -> "Slots: 6 PM - 7 PM"
                BookingType.EVENT -> "1 ticket"
            },
            imageUrl = item?.imageUrl,
            baseAmount = amount,
            convenienceFee = 30,
            taxes = 18
        )
    }
    fun getTicket(booking: Booking): TicketDetails {
        val item = getItemById(booking.itemId)
        return TicketDetails(
            bookingId = "EMS-${booking.id.removePrefix("booking_").padStart(6, '0')}",
            title = item?.title ?: "Booking",
            category = booking.type.name,
            venue = getBookingVenue(booking),
            date = booking.dateTime.substringBefore(" • "),
            time = booking.dateTime.substringAfter(" • ", "Confirmed"),
            admission = booking.details,
            attendee = currentUser.fullName,
            amount = booking.price
        )
    }

    private fun hourLabel(hour: Int): String = when {
        hour == 0 -> "12 AM"
        hour < 12 -> "$hour AM"
        hour == 12 -> "12 PM"
        else -> "${hour - 12} PM"
    }
}
