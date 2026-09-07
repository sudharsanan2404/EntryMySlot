package com.entrymyslotbe.app.network

object NetworkConstants {
    // ==================== BASE URLS ====================
    // /api/ returns a JSON-only 301 directing clients to the versioned routes.
    const val BASE_URL = "https://api.entrymyslot.com/api/v1/"
    const val LEGACY_BASE_URL = "https://api.entrymyslot.com/api/"

    // ==================== TIMEOUTS ====================
    const val CONNECT_TIMEOUT_SEC = 30L
    const val READ_TIMEOUT_SEC = 30L
    const val WRITE_TIMEOUT_SEC = 30L

    // ==================== PREF KEYS ====================
    const val PREF_NAME = "entry_my_slot_prefs"
    const val PREF_USER_TOKEN = "user_access_token"
    const val PREF_USER_REFRESH = "user_refresh_token"
    const val PREF_ADMIN_TOKEN = "admin_access_token"
    const val PREF_ORGANIZER_TOKEN = "organizer_access_token"
    const val PREF_IS_LOGGED_IN = "is_logged_in"
    const val PREF_LOGIN_TYPE = "login_type"
    const val PREF_USER_ID = "user_id"
    const val PREF_USER_NAME = "user_name"
    const val PREF_USER_EMAIL = "user_email"

    // ==================== LOGIN TYPES ====================
    const val LOGIN_TYPE_NONE = "none"
    const val LOGIN_TYPE_USER = "user"
    const val LOGIN_TYPE_ADMIN = "admin"
    const val LOGIN_TYPE_ORGANIZER = "organizer"

    // ==================== ERROR MESSAGES ====================
    const val ERROR_NO_INTERNET = "No internet connection"
    const val ERROR_TIMEOUT = "Request timeout"
    const val ERROR_UNAUTHORIZED = "Session expired. Please login again."
    const val ERROR_SERVER = "Server error. Please try again later."
    const val ERROR_UNKNOWN = "Something went wrong. Please try again."

    // ==================== PAGINATION ====================
    const val DEFAULT_PAGE_SIZE = 20

    // ==================== ENDPOINT SUFFIXES ====================
    object Endpoints {
        // Auth
        const val REGISTER = "auth/register"
        const val LOGIN = "auth/login"
        const val LOGOUT = "auth/logout"
        const val REFRESH_TOKEN = "auth/refresh-token"
        const val FORGOT_PASSWORD = "auth/forgot-password"
        const val RESET_PASSWORD = "auth/reset-password"
        const val GET_ME = "auth/me"
        const val CHANGE_PASSWORD = "auth/change-password"

        // Movies
        // The movie router itself is mounted at /movies, so its local /movies,
        // /cinemas, /showtimes, /bookings and /tickets routes all keep this prefix.
        const val MOVIES = "movies/movies"
        const val MOVIES_SEARCH = "movies/movies/search"
        const val MOVIES_FEATURED = "movies/movies/featured"
        const val MOVIES_GENRES = "movies/movies/genres"
        const val MOVIES_LANGUAGES = "movies/movies/languages"
        const val CINEMAS = "movies/cinemas"
        const val CINEMAS_BY_CITY = "movies/cinemas/city/{city}"
        const val SHOWTIMES = "movies/showtimes"
        const val SHOWTIMES_CITIES = "movies/showtimes/cities"
        const val SHOWTIMES_SEATS = "movies/showtimes/{showtimeId}/seats"
        const val CALCULATE_PRICES = "movies/showtimes/{showtimeId}/calculate-prices"
        const val HOLD_SEATS = "movies/hold-seats"
        const val HOLD_SEATS_STATUS = "movies/hold-seats/{holdKey}/status"
        const val HOLD_SEATS_RELEASE = "movies/hold-seats/{holdKey}/release"

        // Events
        const val EVENTS = "events"
        const val EVENTS_FEATURED = "events/featured"
        const val EVENTS_CATEGORIES = "events/categories"
        const val EVENTS_CITIES = "events/cities"
        const val EVENTS_STATS = "events/{id}/stats"
        const val EVENTS_ZONES = "events/{id}/zones"
        const val EVENTS_BOOKINGS = "events/{id}/bookings"

        // Turf
        const val GROUNDS = "turf/grounds"
        const val RESOURCES_AVAILABILITY = "turf/resources/{resourceId}/availability"
        const val MY_BOOKINGS = "turf/my/bookings"
        const val TURF_BOOKINGS = "turf/bookings"

        // Payments
        const val PAYMENTS_CREATE_ORDER = "turf/payments/create-order"
        const val PAYMENTS_VERIFY = "turf/payments/verify"

        // Tickets
        const val SCAN_VERIFY = "scan/verify"
        const val SCAN_CHECKIN = "scan/mark"
        const val TICKET_VERIFY = "movies/tickets/{ticketUuid}/verify"
        const val TICKET_DETAILS = "movies/tickets/{ticketUuid}/details"

        // Admin
        const val ADMIN_LOGIN = "admin/login"
        const val ADMIN_ME = "admin/me"
        const val ADMIN_STATS = "admin/stats"
        const val ADMIN_BOOKINGS = "admin/bookings"
        const val ADMIN_USERS = "admin/users"
        const val ADMIN_ADMINS = "admin/admins"

        // Organizer
        const val ORGANIZER_LOGIN = "organizer/auth/login"
        const val ORGANIZER_REFRESH = "organizer/auth/refresh"
        const val ORGANIZER_APPLY = "organizer/applications"

        // Promotions
        const val PROMO_PACKAGES = "promotions/packages"
        const val PROMO_SPONSORED = "promotions/sponsored"
        const val PROMO_CLICKS = "promotions/clicks"

        // Health
        // Health probes are deliberately unversioned in server.ts.
        const val HEALTH_LIVE = "https://api.entrymyslot.com/health/live"
        const val HEALTH_READY = "https://api.entrymyslot.com/health/ready"
    }
}
