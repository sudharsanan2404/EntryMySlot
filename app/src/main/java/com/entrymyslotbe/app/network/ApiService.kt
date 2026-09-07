package com.entrymyslotbe.app.network

import com.entrymyslotbe.app.network.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ==================== AUTH ====================
    @POST(NetworkConstants.Endpoints.REGISTER)
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<RegistrationStart>>

    @POST("auth/register-enhanced")
    suspend fun registerEnhanced(@Body request: RegisterRequest): Response<ApiResponse<RegistrationStart>>

    @POST("auth/register-otp")
    suspend fun requestRegistrationOtp(@Body request: RegisterRequest): Response<ApiResponse<RegistrationStart>>

    @POST("auth/verify-registration-otp")
    suspend fun verifyRegistrationOtp(@Body request: VerifyRegistrationOtpRequest): Response<ApiResponse<LoginResponse>>

    @POST("auth/resend-registration-otp")
    suspend fun resendRegistrationOtp(@Body request: EmailRequest): Response<ApiResponse<Any>>

    @POST("auth/login-enhanced")
    suspend fun loginEnhanced(@Body request: DeviceLoginRequest): Response<ApiResponse<LoginResponse>>

    @GET("auth/verify-email")
    suspend fun verifyEmail(@Query("token") token: String): Response<ApiResponse<Any>>

    @POST("auth/resend-verification")
    suspend fun resendVerification(@Body request: EmailRequest): Response<ApiResponse<Any>>

    @POST(NetworkConstants.Endpoints.LOGIN)
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginResponse>>

    @POST(NetworkConstants.Endpoints.LOGOUT)
    suspend fun logout(@Body request: RefreshTokenRequest): Response<ApiResponse<Any>>

    @POST("auth/logout-all")
    suspend fun logoutAll(): Response<ApiResponse<Any>>

    @POST(NetworkConstants.Endpoints.REFRESH_TOKEN)
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<ApiResponse<LoginResponse>>

    @POST(NetworkConstants.Endpoints.FORGOT_PASSWORD)
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ApiResponse<Any>>

    @POST(NetworkConstants.Endpoints.RESET_PASSWORD)
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ApiResponse<Any>>

    @GET(NetworkConstants.Endpoints.GET_ME)
    suspend fun getMe(): Response<ApiResponse<User>>

    @PATCH(NetworkConstants.Endpoints.GET_ME)
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<ApiResponse<User>>

    @POST(NetworkConstants.Endpoints.CHANGE_PASSWORD)
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ApiResponse<Any>>

    @GET("auth/sessions")
    suspend fun getMySessions(): Response<ApiResponse<List<Session>>>

    @POST("auth/sessions/revoke")
    suspend fun revokeMySession(@Body request: RevokeSessionRequest): Response<ApiResponse<Any>>

    // ==================== MOVIES ====================
    @GET(NetworkConstants.Endpoints.MOVIES)
    suspend fun listMovies(@QueryMap filters: Map<String, String> = emptyMap()): Response<PaginatedResponse<Movie>>

    @GET(NetworkConstants.Endpoints.MOVIES_SEARCH)
    suspend fun searchMovies(@Query("q") query: String): Response<ApiResponse<MovieSearchPage>>

    @GET(NetworkConstants.Endpoints.MOVIES_FEATURED)
    suspend fun getFeaturedMovies(): Response<ApiResponse<List<Movie>>>

    @GET(NetworkConstants.Endpoints.MOVIES_GENRES)
    suspend fun getGenres(): Response<ApiResponse<List<String>>>

    @GET(NetworkConstants.Endpoints.MOVIES_LANGUAGES)
    suspend fun getLanguages(): Response<ApiResponse<List<String>>>

    @GET("movies/movies/{slugOrId}")
    suspend fun getMovie(@Path("slugOrId") slugOrId: String): Response<ApiResponse<Movie>>

    // ==================== CINEMAS ====================
    @GET(NetworkConstants.Endpoints.CINEMAS)
    suspend fun listCinemas(@QueryMap filters: Map<String, String> = emptyMap()): Response<ApiResponse<List<Cinema>>>

    @GET("movies/cinemas/city/{city}")
    suspend fun getCinemasByCity(@Path("city") city: String): Response<ApiResponse<List<Cinema>>>

    @GET("movies/cinemas/{idOrSlug}")
    suspend fun getCinema(@Path("idOrSlug") idOrSlug: String): Response<ApiResponse<Cinema>>

    @GET("movies/cinemas/{cinemaId}/screens")
    suspend fun getScreens(@Path("cinemaId") cinemaId: String): Response<ApiResponse<List<Screen>>>

    // ==================== SHOWTIMES ====================
    @GET(NetworkConstants.Endpoints.SHOWTIMES)
    suspend fun listShowtimes(@QueryMap filters: Map<String, String> = emptyMap()): Response<ApiResponse<List<Showtime>>>

    @GET(NetworkConstants.Endpoints.SHOWTIMES_CITIES)
    suspend fun getCitiesWithMovies(): Response<ApiResponse<List<String>>>

    @GET("movies/showtimes/{idOrSlug}")
    suspend fun getShowtime(@Path("idOrSlug") idOrSlug: String): Response<ApiResponse<Showtime>>

    @GET("movies/showtimes/{showtimeId}/seats")
    suspend fun getSeats(@Path("showtimeId") showtimeId: String): Response<ApiResponse<SeatsResponse>>

    @POST("movies/showtimes/{showtimeId}/calculate-prices")
    suspend fun calculatePrices(@Path("showtimeId") showtimeId: String, @Body request: CalculatePricesRequest): Response<ApiResponse<List<PriceBreakdown>>>

    @POST(NetworkConstants.Endpoints.HOLD_SEATS)
    suspend fun holdSeats(@Query("showtimeId") showtimeId: String, @Body request: HoldSeatsRequest): Response<ApiResponse<HoldSeatsResponse>>

    @GET("movies/hold-seats/{holdKey}/status")
    suspend fun checkHold(@Path("holdKey") holdKey: String): Response<ApiResponse<HoldSeatsResponse>>

    @POST("movies/hold-seats/{holdKey}/release")
    suspend fun releaseSeats(@Path("holdKey") holdKey: String): Response<ApiResponse<Any>>

    // ==================== MOVIE BOOKINGS ====================
    @POST("movies/bookings")
    suspend fun createMovieBooking(@Body request: MovieBookingRequest): Response<ApiResponse<MovieBookingResponse>>

    @POST("movies/bookings/confirm")
    suspend fun confirmBooking(@Body request: Map<String, String>): Response<ApiResponse<Booking>>

    @GET("movies/bookings/my")
    suspend fun getMyBookings(@QueryMap filters: Map<String, String> = emptyMap()): Response<PaginatedResponse<Booking>>

    @GET("movies/bookings/{referenceOrId}")
    suspend fun getBooking(@Path("referenceOrId") referenceOrId: String): Response<ApiResponse<Booking>>

    @POST("movies/bookings/{referenceOrId}/cancel")
    suspend fun cancelBooking(@Path("referenceOrId") referenceOrId: String): Response<ApiResponse<Any>>

    @GET("movies/bookings/{referenceOrId}/tickets")
    suspend fun getMyTickets(@Path("referenceOrId") referenceOrId: String): Response<ApiResponse<List<Ticket>>>

    // ==================== EVENTS ====================
    @GET(NetworkConstants.Endpoints.EVENTS)
    suspend fun listEvents(@QueryMap filters: Map<String, String> = emptyMap()): Response<PaginatedResponse<Event>>

    @GET(NetworkConstants.Endpoints.EVENTS_FEATURED)
    suspend fun getFeaturedEvents(): Response<ApiResponse<List<Event>>>

    @GET("events/categories")
    suspend fun getEventCategories(): Response<ApiResponse<List<Category>>>

    @GET("events/cities")
    suspend fun getEventCities(): Response<ApiResponse<List<City>>>

    @GET("events/{id}/stats")
    suspend fun getEventStats(@Path("id") eventId: String): Response<ApiResponse<EventStats>>

    @GET("events/{id}")
    suspend fun getEvent(@Path("id") eventId: String): Response<ApiResponse<Event>>

    @GET("events/{id}/zones")
    suspend fun listZones(@Path("id") eventId: String): Response<ZonesResponse>

    @GET("events/{id}/zones/{zoneId}")
    suspend fun getZone(@Path("id") eventId: String, @Path("zoneId") zoneId: String): Response<ZoneResponse>

    @POST("bookings")
    suspend fun createEventBooking(@Body request: EventBookingRequest): Response<ApiResponse<EventBookingResponse>>

    @POST("bookings/{bookingId}/cancel")
    suspend fun cancelEventBooking(@Path("bookingId") bookingId: String): Response<ApiResponse<Any>>

    @GET("bookings/my")
    suspend fun getMyEventBookings(): Response<ApiResponse<List<EventBooking>>>

    @GET("bookings/{bookingId}")
    suspend fun getEventBookingDetails(@Path("bookingId") bookingId: String): Response<ApiResponse<EventBookingDetails>>

    @GET("bookings/{bookingId}/pdf")
    suspend fun getEventBookingPdf(@Path("bookingId") bookingId: String): Response<ResponseBody>

    @POST("bookings/{bookingId}/verify")
    suspend fun verifyEventBookingPayment(@Path("bookingId") bookingId: String, @Body request: RequestBody): Response<ApiResponse<EventBooking>>

    // ==================== TURF ====================
    @GET(NetworkConstants.Endpoints.GROUNDS)
    suspend fun listVenues(@QueryMap filters: Map<String, String> = emptyMap()): Response<PaginatedResponse<Venue>>

    @GET("turf/grounds/{venueId}")
    suspend fun getVenue(@Path("venueId") venueId: String): Response<ApiResponse<Venue>>

    @GET("turf/grounds/{venueId}/reviews")
    suspend fun listVenueReviews(@Path("venueId") venueId: String): Response<ApiResponse<List<Review>>>

    @GET(NetworkConstants.Endpoints.RESOURCES_AVAILABILITY)
    suspend fun getResourceAvailability(@Path("resourceId") resourceId: String, @Query("date") date: String): Response<ApiResponse<ResourceAvailability>>

    @POST("turf/bookings")
    suspend fun createTurfBooking(@Body request: TurfBookingRequest): Response<ApiResponse<TurfBookingResponse>>

    @GET("turf/my/bookings")
    suspend fun getMyTurfBookings(@QueryMap filters: Map<String, String> = emptyMap()): Response<ApiResponse<TurfBookingPage>>

    @GET("turf/my/bookings/{id}")
    suspend fun getTurfBooking(@Path("id") bookingId: String): Response<ApiResponse<TurfBooking>>

    @POST("turf/my/bookings/{id}/cancel")
    suspend fun cancelTurfBooking(@Path("id") bookingId: String): Response<ApiResponse<Any>>

    @POST("turf/my/bookings/{bookingId}/checkin")
    suspend fun checkInBooking(@Path("bookingId") bookingId: String, @Body request: RequestBody): Response<ApiResponse<Any>>

    @POST("turf/my/bookings/{bookingId}/review")
    suspend fun createReview(@Path("bookingId") bookingId: String, @Body request: Map<String, Any>): Response<ApiResponse<Review>>

    // ==================== PAYMENTS ====================
    @POST(NetworkConstants.Endpoints.PAYMENTS_CREATE_ORDER)
    suspend fun createPaymentOrder(@Body request: PaymentOrderRequest): Response<ApiResponse<PaymentOrderResponse>>

    @POST(NetworkConstants.Endpoints.PAYMENTS_VERIFY)
    suspend fun verifyPayment(@Body request: PaymentVerifyRequest): Response<ApiResponse<Any>>

    // ==================== TICKETS / SCAN ====================
    @POST(NetworkConstants.Endpoints.SCAN_VERIFY)
    suspend fun scanVerifyTicket(@Body request: Map<String, String>): Response<ApiResponse<TicketVerificationResponse>>

    @POST(NetworkConstants.Endpoints.SCAN_CHECKIN)
    suspend fun scanCheckIn(@Body request: Map<String, String>): Response<ApiResponse<Any>>

    @POST("scan/turf/verify")
    suspend fun scanVerifyTurfTicket(@Body request: Map<String, String>): Response<ApiResponse<TicketVerificationResponse>>

    @POST("scan/turf/mark")
    suspend fun scanTurfCheckIn(@Body request: Map<String, String>): Response<ApiResponse<Any>>

    @POST("scan/movies/verify")
    suspend fun scanVerifyMovieTicket(@Body request: Map<String, String>): Response<ApiResponse<TicketVerificationResponse>>

    @POST("scan/movies/mark")
    suspend fun scanMovieCheckIn(@Body request: Map<String, String>): Response<ApiResponse<Any>>

    @GET("movies/tickets/{ticketUuid}/verify")
    suspend fun verifyTicketGet(@Path("ticketUuid") ticketUuid: String): Response<ApiResponse<TicketVerificationResponse>>

    @GET("movies/tickets/{ticketUuid}/details")
    suspend fun getTicketDetails(@Path("ticketUuid") ticketUuid: String): Response<ApiResponse<Ticket>>

    // ==================== ADMIN ====================
    @POST(NetworkConstants.Endpoints.ADMIN_LOGIN)
    suspend fun adminLogin(@Body request: AdminLoginRequest): Response<ApiResponse<AdminLoginResponse>>

    @GET(NetworkConstants.Endpoints.ADMIN_ME)
    suspend fun adminMe(): Response<ApiResponse<Admin>>

    @GET(NetworkConstants.Endpoints.ADMIN_STATS)
    suspend fun adminStats(): Response<ApiResponse<AdminStats>>

    @GET(NetworkConstants.Endpoints.ADMIN_BOOKINGS)
    suspend fun adminBookings(@QueryMap filters: Map<String, String> = emptyMap()): Response<PaginatedResponse<Booking>>

    @POST("admin/bookings/{id}/cancel")
    suspend fun adminCancelBooking(@Path("id") bookingId: String, @Body request: RequestBody): Response<ApiResponse<Any>>

    @GET("admin/recent-tickets")
    suspend fun adminRecentTickets(@QueryMap filters: Map<String, String> = emptyMap()): Response<ApiResponse<List<Ticket>>>

    @GET(NetworkConstants.Endpoints.ADMIN_USERS)
    suspend fun adminUsers(@QueryMap filters: Map<String, String> = emptyMap()): Response<PaginatedResponse<User>>

    @GET(NetworkConstants.Endpoints.ADMIN_ADMINS)
    suspend fun adminListAdmins(): Response<ApiResponse<List<Admin>>>

    @GET("admin/audit-logs")
    suspend fun adminAuditLogs(@QueryMap filters: Map<String, String> = emptyMap()): Response<PaginatedResponse<AuditLog>>

    @GET("admin/organizer-applications")
    suspend fun adminOrganizerApplications(@QueryMap filters: Map<String, String> = emptyMap()): Response<PaginatedResponse<OrganizerApplication>>

    @GET("admin/organizations")
    suspend fun adminOrganizations(@QueryMap filters: Map<String, String> = emptyMap()): Response<PaginatedResponse<Organization>>

    @GET("admin/organizer-applications/{id}")
    suspend fun adminGetOrganizerApplication(@Path("id") applicationId: String): Response<ApiResponse<OrganizerApplication>>

    @GET("admin/refunds")
    suspend fun adminListRefunds(@QueryMap filters: Map<String, String> = emptyMap()): Response<PaginatedResponse<Refund>>

    @GET("admin/refunds/{id}")
    suspend fun adminGetRefund(@Path("id") refundId: String): Response<ApiResponse<Refund>>

    @POST("admin/refunds")
    suspend fun adminCreateRefund(@Body request: CreateRefundRequest): Response<ApiResponse<Refund>>

    // Admin Events
    @GET("admin/events")
    suspend fun adminListEvents(@QueryMap filters: Map<String, String> = emptyMap()): Response<PaginatedResponse<Event>>

    @POST("admin/events")
    suspend fun adminCreateEvent(@Body request: RequestBody): Response<ApiResponse<Event>>

    @PATCH("admin/events/{id}")
    suspend fun adminUpdateEvent(@Path("id") eventId: String, @Body request: RequestBody): Response<ApiResponse<Event>>

    @PUT("admin/events/{id}")
    suspend fun adminReplaceEvent(@Path("id") eventId: String, @Body request: RequestBody): Response<ApiResponse<Event>>

    @DELETE("admin/events/{id}")
    suspend fun adminDeleteEvent(@Path("id") eventId: String): Response<ApiResponse<Any>>

    @POST("admin/events/{id}/restore")
    suspend fun adminRestoreEvent(@Path("id") eventId: String): Response<ApiResponse<Event>>

    @POST("admin/events/{id}/publish")
    suspend fun adminPublishEvent(@Path("id") eventId: String): Response<ApiResponse<Event>>

    @POST("admin/events/{id}/hide")
    suspend fun adminHideEvent(@Path("id") eventId: String): Response<ApiResponse<Event>>

    @POST("admin/events/{id}/cancel")
    suspend fun adminCancelEvent(@Path("id") eventId: String): Response<ApiResponse<Event>>

    @POST("admin/events/{id}/featured")
    suspend fun adminSetFeatured(@Path("id") eventId: String): Response<ApiResponse<Event>>

    @POST("admin/events/{id}/submit-for-review")
    suspend fun submitForReview(@Path("id") eventId: String): Response<ApiResponse<Event>>

    @POST("admin/events/{id}/approve")
    suspend fun approveEvent(@Path("id") eventId: String): Response<ApiResponse<Event>>

    @POST("admin/events/{id}/reject")
    suspend fun rejectEvent(@Path("id") eventId: String): Response<ApiResponse<Event>>

    @POST("admin/events/{id}/unpublish")
    suspend fun unpublishEvent(@Path("id") eventId: String): Response<ApiResponse<Event>>

    @POST("admin/events/{id}/show")
    suspend fun showEvent(@Path("id") eventId: String): Response<ApiResponse<Event>>

    @POST("admin/events/{id}/archive")
    suspend fun archiveEvent(@Path("id") eventId: String): Response<ApiResponse<Event>>

    @GET("admin/events/{id}/history")
    suspend fun getEventHistory(@Path("id") eventId: String): Response<ApiResponse<List<AuditLog>>>

    @GET("admin/events/pending-review")
    suspend fun listPendingReview(): Response<PaginatedResponse<Event>>

    @POST("admin/events/{id}/zones")
    suspend fun adminCreateZone(@Path("id") eventId: String, @Body request: RequestBody): Response<ZoneResponse>

    @PATCH("admin/events/{id}/zones/{zoneId}")
    suspend fun adminUpdateZone(@Path("id") eventId: String, @Path("zoneId") zoneId: String, @Body request: RequestBody): Response<ZoneResponse>

    @DELETE("admin/events/{id}/zones/{zoneId}")
    suspend fun adminDeleteZone(@Path("id") eventId: String, @Path("zoneId") zoneId: String): Response<ApiResponse<Any>>

    @GET("admin/events/{id}/zones/{zoneId}/availability")
    suspend fun adminGetAvailability(@Path("id") eventId: String, @Path("zoneId") zoneId: String): Response<ZonesResponse>

    // Admin Movies
    @GET("admin/movies/movies")
    suspend fun adminListMovies(@QueryMap filters: Map<String, String> = emptyMap()): Response<PaginatedResponse<Movie>>

    @POST("admin/movies/movies")
    suspend fun adminCreateMovie(@Body request: RequestBody): Response<ApiResponse<Movie>>

    @PATCH("admin/movies/movies/{id}")
    suspend fun adminUpdateMovie(@Path("id") movieId: String, @Body request: RequestBody): Response<ApiResponse<Movie>>

    @DELETE("admin/movies/movies/{id}")
    suspend fun adminDeleteMovie(@Path("id") movieId: String): Response<ApiResponse<Any>>

    @POST("admin/movies/movies/{id}/publish")
    suspend fun adminPublishMovie(@Path("id") movieId: String): Response<ApiResponse<Movie>>

    @POST("admin/movies/movies/{id}/archive")
    suspend fun adminArchiveMovie(@Path("id") movieId: String): Response<ApiResponse<Movie>>

    @GET("admin/movies/cinemas")
    suspend fun adminListCinemas(@QueryMap filters: Map<String, String> = emptyMap()): Response<PaginatedResponse<Cinema>>

    @POST("admin/movies/cinemas")
    suspend fun adminCreateCinema(@Body request: RequestBody): Response<ApiResponse<Cinema>>

    @PATCH("admin/movies/cinemas/{id}")
    suspend fun adminUpdateCinema(@Path("id") cinemaId: String, @Body request: RequestBody): Response<ApiResponse<Cinema>>

    @DELETE("admin/movies/cinemas/{id}")
    suspend fun adminDeleteCinema(@Path("id") cinemaId: String): Response<ApiResponse<Any>>

    @POST("admin/movies/cinemas/{id}/toggle")
    suspend fun adminToggleCinema(@Path("id") cinemaId: String): Response<ApiResponse<Cinema>>

    @POST("admin/movies/cinemas/{cinemaId}/screens")
    suspend fun adminCreateScreen(@Path("cinemaId") cinemaId: String, @Body request: RequestBody): Response<ApiResponse<Screen>>

    @PATCH("admin/movies/screens/{id}")
    suspend fun adminUpdateScreen(@Path("id") screenId: String, @Body request: RequestBody): Response<ApiResponse<Screen>>

    @DELETE("admin/movies/screens/{id}")
    suspend fun adminDeleteScreen(@Path("id") screenId: String): Response<ApiResponse<Any>>

    @GET("admin/movies/showtimes")
    suspend fun adminListShowtimes(@QueryMap filters: Map<String, String> = emptyMap()): Response<PaginatedResponse<Showtime>>

    @POST("admin/movies/showtimes")
    suspend fun adminCreateShowtime(@Body request: RequestBody): Response<ApiResponse<Showtime>>

    @PATCH("admin/movies/showtimes/{id}")
    suspend fun adminUpdateShowtime(@Path("id") showtimeId: String, @Body request: RequestBody): Response<ApiResponse<Showtime>>

    @DELETE("admin/movies/showtimes/{id}")
    suspend fun adminDeleteShowtime(@Path("id") showtimeId: String): Response<ApiResponse<Any>>

    @GET("admin/movies/cinemas/{cinemaId}/showtimes")
    suspend fun adminCinemaShowtimes(@Path("cinemaId") cinemaId: String): Response<ApiResponse<List<Showtime>>>

    @GET("admin/movies/movies/{movieId}/showtimes")
    suspend fun adminMovieShowtimes(@Path("movieId") movieId: String): Response<ApiResponse<List<Showtime>>>

    @GET("admin/movies/showtimes/stats")
    suspend fun adminShowtimeStats(): Response<ApiResponse<Map<String, Any>>>

    @GET("admin/movies/price-caps")
    suspend fun adminListPriceCaps(): Response<ApiResponse<List<Any>>>

    @POST("admin/movies/price-caps")
    suspend fun adminCreatePriceCap(@Body request: RequestBody): Response<ApiResponse<Any>>

    @PATCH("admin/movies/price-caps/{id}")
    suspend fun adminUpdatePriceCap(@Path("id") id: String, @Body request: RequestBody): Response<ApiResponse<Any>>

    @DELETE("admin/movies/price-caps/{id}")
    suspend fun adminDeletePriceCap(@Path("id") id: String): Response<ApiResponse<Any>>

    @GET("admin/layout-versions/screen/{screenId}")
    suspend fun adminLayoutVersions(@Path("screenId") screenId: String): Response<ApiResponse<List<LayoutVersion>>>

    @GET("admin/layout-versions/screen/{screenId}/current")
    suspend fun adminCurrentLayout(@Path("screenId") screenId: String): Response<ApiResponse<LayoutVersion>>

    @GET("admin/layout-versions/{id}")
    suspend fun adminGetLayoutVersion(@Path("id") layoutId: String): Response<ApiResponse<LayoutVersion>>

    @GET("admin/layout-versions/{id}/seats")
    suspend fun adminGetLayoutSeats(@Path("id") layoutId: String): Response<ApiResponse<List<Seat>>>

    @POST("admin/layout-versions")
    suspend fun adminCreateLayoutVersion(@Body request: RequestBody): Response<ApiResponse<LayoutVersion>>

    @POST("admin/layout-versions/screen/{screenId}/new-version")
    suspend fun adminCreateLayoutForScreen(@Path("screenId") screenId: String, @Body request: RequestBody): Response<ApiResponse<LayoutVersion>>

    @PATCH("admin/layout-versions/{id}/set-current")
    suspend fun adminSetCurrentLayout(@Path("id") layoutId: String): Response<ApiResponse<LayoutVersion>>

    @POST("admin/layout-versions/{id}/seats")
    suspend fun adminAddSeatsToLayout(@Path("id") layoutId: String, @Body request: RequestBody): Response<ApiResponse<LayoutVersion>>

    @POST("admin/layout-versions/{id}/sync-seats")
    suspend fun adminSyncSeats(@Path("id") layoutId: String): Response<ApiResponse<LayoutVersion>>

    @DELETE("admin/layout-versions/{id}")
    suspend fun adminDeleteLayoutVersion(@Path("id") layoutId: String): Response<ApiResponse<Any>>

    @POST("admin/layout-versions/screen/{screenId}/initialize")
    suspend fun adminInitializeLayout(@Path("screenId") screenId: String): Response<ApiResponse<LayoutVersion>>

    // Admin Turf
    @GET("turf/admin/venues")
    suspend fun adminListAllVenues(@QueryMap filters: Map<String, String> = emptyMap()): Response<PaginatedResponse<Venue>>

    @PATCH("turf/admin/venues/{venueId}/status")
    suspend fun adminUpdateVenueStatus(@Path("venueId") venueId: String, @Body request: RequestBody): Response<ApiResponse<Venue>>

    @GET("turf/admin/bookings")
    suspend fun adminListAllBookings(@QueryMap filters: Map<String, String> = emptyMap()): Response<PaginatedResponse<TurfBooking>>

    @GET("turf/admin/bookings/{id}")
    suspend fun adminGetBookingDetail(@Path("id") bookingId: String): Response<ApiResponse<TurfBooking>>

    @GET("turf/admin/venues/{venueId}/reviews")
    suspend fun adminVenueReviews(@Path("venueId") venueId: String): Response<ApiResponse<List<Review>>>

    // Admin Manager/Org
    @POST("admin/organizer-applications/{id}/review")
    suspend fun adminReviewApplication(@Path("id") appId: String, @Body request: RequestBody): Response<ApiResponse<OrganizerApplication>>

    @GET("admin/organizations/{id}")
    suspend fun adminGetOrganization(@Path("id") orgId: String): Response<ApiResponse<Organization>>

    @PATCH("admin/organizations/{id}")
    suspend fun adminUpdateOrganization(@Path("id") orgId: String, @Body request: RequestBody): Response<ApiResponse<Organization>>

    @POST("admin/organizations/{id}/deactivate")
    suspend fun adminDeactivateOrganization(@Path("id") orgId: String): Response<ApiResponse<Organization>>

    @POST("admin/organizations/{id}/reactivate")
    suspend fun adminReactivateOrganization(@Path("id") orgId: String): Response<ApiResponse<Organization>>

    @GET("admin/managers")
    suspend fun adminListManagers(@QueryMap filters: Map<String, String> = emptyMap()): Response<PaginatedResponse<Manager>>

    @GET("admin/managers/{id}")
    suspend fun adminGetManager(@Path("id") managerId: String): Response<ApiResponse<Manager>>

    @POST("admin/managers")
    suspend fun adminCreateManager(@Body request: CreateManagerRequest): Response<ApiResponse<Manager>>

    @PATCH("admin/managers/{id}")
    suspend fun adminUpdateManager(@Path("id") managerId: String, @Body request: UpdateManagerRequest): Response<ApiResponse<Manager>>

    @POST("admin/managers/{id}/deactivate")
    suspend fun adminDeactivateManager(@Path("id") managerId: String): Response<ApiResponse<Manager>>

    @POST("admin/managers/{id}/reactivate")
    suspend fun adminReactivateManager(@Path("id") managerId: String): Response<ApiResponse<Manager>>

    // ==================== ORGANIZER ====================
    @POST(NetworkConstants.Endpoints.ORGANIZER_LOGIN)
    suspend fun organizerLogin(@Body request: OrganizerLoginRequest): Response<ApiResponse<OrganizerLoginResponse>>

    @POST(NetworkConstants.Endpoints.ORGANIZER_REFRESH)
    suspend fun organizerRefresh(@Body request: RefreshTokenRequest): Response<ApiResponse<OrganizerLoginResponse>>

    @POST("organizer/auth/setup-password")
    suspend fun organizerSetupPassword(@Body request: Map<String, String>): Response<ApiResponse<OrganizerLoginResponse>>

    @POST(NetworkConstants.Endpoints.ORGANIZER_APPLY)
    suspend fun submitOrganizerApplication(@Body request: Map<String, String>): Response<ApiResponse<Any>>

    @GET("organizer/applications/status")
    suspend fun getApplicationStatus(@Query("email") email: String): Response<ApiResponse<OrganizerApplication>>

    // Organizer Events
    @GET("organizer/events")
    suspend fun ownerListEvents(@QueryMap filters: Map<String, String> = emptyMap()): Response<PaginatedResponse<Event>>

    @GET("organizer/events/{id}")
    suspend fun ownerGetEvent(@Path("id") eventId: String): Response<ApiResponse<Event>>

    @POST("organizer/events")
    suspend fun ownerCreateEvent(@Body request: RequestBody): Response<ApiResponse<Event>>

    @PATCH("organizer/events/{id}")
    suspend fun ownerUpdateEvent(@Path("id") eventId: String, @Body request: RequestBody): Response<ApiResponse<Event>>

    @DELETE("organizer/events/{id}")
    suspend fun ownerDeleteEvent(@Path("id") eventId: String): Response<ApiResponse<Any>>

    @GET("organizer/events/{id}/ticket-tiers")
    suspend fun ownerGetTicketTiers(@Path("id") eventId: String): Response<ApiResponse<List<TicketTier>>>

    @GET("organizer/events/{id}/seats")
    suspend fun ownerGetEventSeats(@Path("id") eventId: String): Response<ApiResponse<List<Zone>>>

    @POST("organizer/events/{id}/seats")
    suspend fun ownerCreateEventSeats(@Path("id") eventId: String, @Body request: RequestBody): Response<ApiResponse<Zone>>

    // Deployed organizer-zone routes were found on the test server but omitted
    // from the supplied audit document.
    @GET("organizer/events/{id}/zones")
    suspend fun ownerListEventZones(@Path("id") eventId: String): Response<ZonesResponse>

    @GET("organizer/events/{id}/zones/{zoneId}")
    suspend fun ownerGetEventZone(@Path("id") eventId: String, @Path("zoneId") zoneId: String): Response<ZoneResponse>

    @POST("organizer/events/{id}/zones")
    suspend fun ownerCreateEventZone(@Path("id") eventId: String, @Body request: RequestBody): Response<ZoneResponse>

    @PUT("organizer/events/zones/{zoneId}")
    suspend fun ownerUpdateEventZone(@Path("zoneId") zoneId: String, @Body request: RequestBody): Response<ZoneResponse>

    @DELETE("organizer/events/zones/{zoneId}")
    suspend fun ownerDeleteEventZone(@Path("zoneId") zoneId: String): Response<ApiResponse<Any>>

    // Organizer Organization
    @GET("organizer/organizations/me")
    suspend fun getOwnOrganization(): Response<ApiResponse<Organization>>

    @GET("organizer/organizations/{id}")
    suspend fun getOrganization(@Path("id") orgId: String): Response<ApiResponse<Organization>>

    @PATCH("organizer/organizations/{id}")
    suspend fun updateOrganization(@Path("id") orgId: String, @Body request: RequestBody): Response<ApiResponse<Organization>>

    @PATCH("organizer/organizations/{id}/banking")
    suspend fun updateBanking(@Path("id") orgId: String, @Body request: BankingInfo): Response<ApiResponse<Organization>>

    @POST("organizer/organizations/{id}/deactivate")
    suspend fun deactivateOrganization(@Path("id") orgId: String): Response<ApiResponse<Organization>>

    @POST("organizer/organizations/{id}/reactivate")
    suspend fun reactivateOrganization(@Path("id") orgId: String): Response<ApiResponse<Organization>>

    // Organizer Invitations
    @POST("owner/invitations")
    suspend fun createInvitation(@Body request: CreateInvitationRequest): Response<ApiResponse<Invitation>>

    @GET("owner/invitations")
    suspend fun listInvitations(@QueryMap filters: Map<String, String> = emptyMap()): Response<PaginatedResponse<Invitation>>

    @GET("owner/invitations/{id}")
    suspend fun getInvitation(@Path("id") invitationId: String): Response<ApiResponse<Invitation>>

    @POST("owner/invitations/{id}/resend")
    suspend fun resendInvitation(@Path("id") invitationId: String): Response<ApiResponse<Invitation>>

    @POST("owner/invitations/{id}/revoke")
    suspend fun revokeInvitation(@Path("id") invitationId: String): Response<ApiResponse<Invitation>>

    @GET("invitations/verify/{token}")
    suspend fun verifyInvitationToken(@Path("token") token: String): Response<ApiResponse<Invitation>>

    @POST("invitations/accept")
    suspend fun acceptInvitation(@Body request: RequestBody): Response<ApiResponse<Any>>

    // ==================== MANAGER ====================
    @GET("owner/managers")
    suspend fun listManagers(@QueryMap filters: Map<String, String> = emptyMap()): Response<PaginatedResponse<Manager>>

    @GET("owner/managers/{id}")
    suspend fun getManager(@Path("id") managerId: String): Response<ApiResponse<Manager>>

    @POST("owner/managers")
    suspend fun createManager(@Body request: CreateManagerRequest): Response<ApiResponse<Manager>>

    @PATCH("owner/managers/{id}")
    suspend fun updateManager(@Path("id") managerId: String, @Body request: UpdateManagerRequest): Response<ApiResponse<Manager>>

    @POST("owner/managers/{id}/disable")
    suspend fun disableManager(@Path("id") managerId: String): Response<ApiResponse<Manager>>

    @POST("owner/managers/{id}/enable")
    suspend fun enableManager(@Path("id") managerId: String): Response<ApiResponse<Manager>>

    @POST("owner/managers/{id}/reset-password")
    suspend fun resetManagerPassword(@Path("id") managerId: String): Response<ApiResponse<Map<String, String>>>

    @DELETE("owner/managers/{id}")
    suspend fun deleteManager(@Path("id") managerId: String): Response<ApiResponse<Any>>

    @GET("owner/managers/analytics")
    suspend fun getManagerAnalytics(@QueryMap params: Map<String, String> = emptyMap()): Response<ApiResponse<Map<String, Any>>>

    // Manager Turf
    @POST("turf/manager/organizations/{orgId}/offline-booking")
    suspend fun createOfflineBooking(@Path("orgId") orgId: String, @Body request: RequestBody): Response<ApiResponse<TurfBookingResponse>>

    @POST("turf/manager/organizations/{orgId}/validate-qr")
    suspend fun validateQR(@Path("orgId") orgId: String, @Body request: Map<String, String>): Response<ApiResponse<Map<String, Any>>>

    @POST("turf/manager/organizations/{orgId}/bookings/{id}/cancel")
    suspend fun managerCancelBooking(@Path("orgId") orgId: String, @Path("id") bookingId: String): Response<ApiResponse<Any>>

    @GET("turf/manager/organizations/{orgId}/attendance")
    suspend fun getAttendanceReport(@Path("orgId") orgId: String, @QueryMap params: Map<String, String> = emptyMap()): Response<ApiResponse<List<Map<String, Any>>>>

    @GET("turf/manager/organizations/{orgId}/daily-report")
    suspend fun getDailyReport(@Path("orgId") orgId: String, @Query("date") date: String): Response<ApiResponse<Map<String, Any>>>

    @GET("turf/manager/organizations/{orgId}/entry-logs")
    suspend fun getEntryLogs(@Path("orgId") orgId: String, @QueryMap filters: Map<String, String> = emptyMap()): Response<PaginatedResponse<Map<String, Any>>>

    // Organizer Turf
    @GET("turf/organizer/bookings")
    suspend fun ownerTurfBookings(@QueryMap filters: Map<String, String> = emptyMap()): Response<PaginatedResponse<TurfBooking>>

    @GET("turf/organizer/venues")
    suspend fun ownerTurfVenues(@QueryMap filters: Map<String, String> = emptyMap()): Response<PaginatedResponse<Venue>>

    @POST("turf/organizer/venues")
    suspend fun ownerCreateVenue(@Body request: RequestBody): Response<ApiResponse<Venue>>

    @GET("turf/organizer/venues/{venueId}")
    suspend fun ownerGetVenue(@Path("venueId") venueId: String): Response<ApiResponse<Venue>>

    @PATCH("turf/organizer/venues/{venueId}")
    suspend fun ownerUpdateVenue(@Path("venueId") venueId: String, @Body request: RequestBody): Response<ApiResponse<Venue>>

    @DELETE("turf/organizer/venues/{venueId}")
    suspend fun ownerDeleteVenue(@Path("venueId") venueId: String): Response<ApiResponse<Any>>

    @GET("turf/organizer/coupons")
    suspend fun listCoupons(@QueryMap filters: Map<String, String> = emptyMap()): Response<PaginatedResponse<Coupon>>

    @POST("turf/organizer/coupons")
    suspend fun createCoupon(@Body request: RequestBody): Response<ApiResponse<Coupon>>

    @GET("turf/organizer/settlements")
    suspend fun listSettlements(@QueryMap filters: Map<String, String> = emptyMap()): Response<PaginatedResponse<Settlement>>

    @GET("turf/organizer/venues/{venueId}/resources")
    suspend fun listResources(@Path("venueId") venueId: String): Response<ApiResponse<List<Resource>>>

    @POST("turf/organizer/venues/{venueId}/resources")
    suspend fun createResource(@Path("venueId") venueId: String, @Body request: RequestBody): Response<ApiResponse<Resource>>

    @GET("turf/organizer/venues/{venueId}/resources/{resourceId}")
    suspend fun getOrganizerResource(@Path("venueId") venueId: String, @Path("resourceId") resourceId: String): Response<ApiResponse<Resource>>

    @PATCH("turf/organizer/venues/{venueId}/resources/{resourceId}")
    suspend fun updateResource(@Path("venueId") venueId: String, @Path("resourceId") resourceId: String, @Body request: RequestBody): Response<ApiResponse<Resource>>

    @DELETE("turf/organizer/venues/{venueId}/resources/{resourceId}")
    suspend fun deleteResource(@Path("venueId") venueId: String, @Path("resourceId") resourceId: String): Response<ApiResponse<Any>>

    @GET("turf/organizer/venues/{venueId}/resources/{resourceId}/slots")
    suspend fun listResourceSlots(@Path("venueId") venueId: String, @Path("resourceId") resourceId: String, @Query("date") date: String): Response<ApiResponse<List<ResourceSlot>>>

    @POST("turf/organizer/venues/{venueId}/resources/{resourceId}/slots")
    suspend fun generateResourceSlots(@Path("venueId") venueId: String, @Path("resourceId") resourceId: String, @Body request: RequestBody): Response<ApiResponse<List<ResourceSlot>>>

    // ==================== PROMOTIONS ====================
    @GET(NetworkConstants.Endpoints.PROMO_PACKAGES)
    suspend fun listPromoPackages(): Response<ApiResponse<List<PromotionPackage>>>

    @GET("promotions/packages/{id}")
    suspend fun getPromoPackage(@Path("id") packageId: String): Response<ApiResponse<PromotionPackage>>

    @GET(NetworkConstants.Endpoints.PROMO_SPONSORED)
    suspend fun getSponsoredResults(@QueryMap params: Map<String, String> = emptyMap()): Response<ApiResponse<SponsoredResults>>

    @POST(NetworkConstants.Endpoints.PROMO_CLICKS)
    suspend fun trackPromoClick(@Body request: TrackClickRequest): Response<ApiResponse<Any>>

    @GET("promotions/organizer/campaigns")
    suspend fun ownerListCampaigns(@QueryMap filters: Map<String, String> = emptyMap()): Response<PaginatedResponse<Campaign>>

    @GET("promotions/organizer/campaigns/{id}")
    suspend fun ownerGetCampaign(@Path("id") campaignId: String): Response<ApiResponse<Campaign>>

    @POST("promotions/organizer/campaigns")
    suspend fun ownerCreateCampaign(@Body request: CreateCampaignRequest): Response<ApiResponse<Campaign>>

    @POST("promotions/organizer/campaigns/{id}/payment")
    suspend fun ownerCreateCampaignPayment(@Path("id") campaignId: String): Response<ApiResponse<PaymentOrderResponse>>

    @POST("promotions/organizer/campaigns/{id}/activate")
    suspend fun activateCampaign(@Path("id") campaignId: String): Response<ApiResponse<Campaign>>

    @POST("promotions/organizer/campaigns/{id}/cancel")
    suspend fun cancelCampaign(@Path("id") campaignId: String): Response<ApiResponse<Campaign>>

    @GET("promotions/organizer/campaigns/{id}/analytics")
    suspend fun getCampaignAnalytics(@Path("id") campaignId: String): Response<ApiResponse<CampaignAnalytics>>

    @GET("promotions/organizer/packages")
    suspend fun ownerPromoPackages(): Response<ApiResponse<List<PromotionPackage>>>

    // Admin Promotions
    @GET("promotions/admin/packages")
    suspend fun adminListPromoPackages(@QueryMap filters: Map<String, String> = emptyMap()): Response<PaginatedResponse<PromotionPackage>>

    @POST("promotions/admin/packages")
    suspend fun adminCreatePromoPackage(@Body request: RequestBody): Response<ApiResponse<PromotionPackage>>

    @PATCH("promotions/admin/packages/{id}")
    suspend fun adminUpdatePromoPackage(@Path("id") packageId: String, @Body request: RequestBody): Response<ApiResponse<PromotionPackage>>

    @DELETE("promotions/admin/packages/{id}")
    suspend fun adminDeletePromoPackage(@Path("id") packageId: String): Response<ApiResponse<Any>>

    @GET("promotions/admin/campaigns")
    suspend fun adminListAllCampaigns(@QueryMap filters: Map<String, String> = emptyMap()): Response<PaginatedResponse<Campaign>>

    @PATCH("promotions/admin/campaigns/{id}/approve")
    suspend fun adminApproveCampaign(@Path("id") campaignId: String): Response<ApiResponse<Campaign>>

    @PATCH("promotions/admin/campaigns/{id}/reject")
    suspend fun adminRejectCampaign(@Path("id") campaignId: String): Response<ApiResponse<Campaign>>

    @PATCH("promotions/admin/campaigns/{id}/pause")
    suspend fun adminPauseCampaign(@Path("id") campaignId: String): Response<ApiResponse<Campaign>>

    @PATCH("promotions/admin/campaigns/{id}/resume")
    suspend fun adminResumeCampaign(@Path("id") campaignId: String): Response<ApiResponse<Campaign>>

    @GET("promotions/admin/analytics")
    suspend fun adminPromoAnalytics(@QueryMap params: Map<String, String> = emptyMap()): Response<ApiResponse<CampaignAnalytics>>

    // ==================== MEDIA / UPLOADS ====================
    @Multipart
    @POST("admin/uploads/event")
    suspend fun uploadEventImage(@Part image: MultipartBody.Part): Response<ApiResponse<Media>>

    @Multipart
    @POST("admin/uploads/banner")
    suspend fun uploadBannerImage(@Part image: MultipartBody.Part): Response<ApiResponse<Media>>

    @POST("admin/media")
    suspend fun uploadMediaBase64(@Body request: UploadMediaRequest): Response<ApiResponse<Media>>

    @GET("admin/media")
    suspend fun listMedia(@QueryMap filters: Map<String, String> = emptyMap()): Response<PaginatedResponse<Media>>

    @GET("admin/media/{id}")
    suspend fun getMedia(@Path("id") mediaId: String): Response<ApiResponse<Media>>

    @PATCH("admin/media/{id}")
    suspend fun updateMedia(@Path("id") mediaId: String, @Body request: RequestBody): Response<ApiResponse<Media>>

    @DELETE("admin/media/{id}")
    suspend fun deleteMedia(@Path("id") mediaId: String): Response<ApiResponse<Any>>

    @POST("admin/media/{id}/restore")
    suspend fun restoreMedia(@Path("id") mediaId: String): Response<ApiResponse<Media>>

    @POST("admin/events/{eventId}/media")
    suspend fun attachEventMedia(@Path("eventId") eventId: String, @Body request: RequestBody): Response<ApiResponse<EventMedia>>

    @GET("admin/events/{eventId}/media")
    suspend fun listEventMedia(@Path("eventId") eventId: String): Response<ApiResponse<List<EventMedia>>>

    @POST("admin/events/{eventId}/media/reorder")
    suspend fun reorderEventMedia(@Path("eventId") eventId: String, @Body request: RequestBody): Response<ApiResponse<Any>>

    @PATCH("admin/events/media/{eventMediaId}")
    suspend fun updateEventMedia(@Path("eventMediaId") eventMediaId: String, @Body request: RequestBody): Response<ApiResponse<EventMedia>>

    @DELETE("admin/events/media/{eventMediaId}")
    suspend fun detachEventMedia(@Path("eventMediaId") eventMediaId: String): Response<ApiResponse<Any>>

    // Banners
    @GET("admin/banners")
    suspend fun listBanners(@QueryMap filters: Map<String, String> = emptyMap()): Response<PaginatedResponse<Banner>>

    @GET("admin/banners/{id}")
    suspend fun getBanner(@Path("id") bannerId: String): Response<ApiResponse<Banner>>

    @Multipart
    @POST("admin/banners")
    suspend fun createBannerFromUpload(@Part image: MultipartBody.Part, @Part("title") title: RequestBody): Response<ApiResponse<Banner>>

    @PATCH("admin/banners/{id}")
    suspend fun updateBanner(@Path("id") bannerId: String, @Body request: RequestBody): Response<ApiResponse<Banner>>

    @DELETE("admin/banners/{id}")
    suspend fun deleteBanner(@Path("id") bannerId: String): Response<ApiResponse<Any>>

    @PUT("admin/banners/{id}/activate")
    suspend fun activateBanner(@Path("id") bannerId: String): Response<ApiResponse<Banner>>

    @PUT("admin/banners/{id}/deactivate")
    suspend fun deactivateBanner(@Path("id") bannerId: String): Response<ApiResponse<Banner>>

    @GET("admin/banners/active-ticket-ad")
    suspend fun getActiveTicketAd(): Response<ApiResponse<TicketAdBanner>>

    // ==================== MOVIE MANAGER (ORGANIZER) ====================
    @GET("organizer/movies/movies")
    suspend fun ownerListMovies(@QueryMap filters: Map<String, String> = emptyMap()): Response<PaginatedResponse<Movie>>

    @GET("organizer/movies/movies/{id}")
    suspend fun ownerGetMovie(@Path("id") movieId: String): Response<ApiResponse<Movie>>

    @POST("organizer/movies/movies")
    suspend fun ownerCreateMovie(@Body request: RequestBody): Response<ApiResponse<Movie>>

    @PATCH("organizer/movies/movies/{id}")
    suspend fun ownerUpdateMovie(@Path("id") movieId: String, @Body request: RequestBody): Response<ApiResponse<Movie>>

    @DELETE("organizer/movies/movies/{id}")
    suspend fun ownerDeleteMovie(@Path("id") movieId: String): Response<ApiResponse<Any>>

    @Multipart
    @POST("organizer/movies/movies/{id}/upload-poster")
    suspend fun uploadMoviePoster(@Path("id") movieId: String, @Part image: MultipartBody.Part): Response<ApiResponse<Movie>>

    @Multipart
    @POST("organizer/movies/movies/{id}/upload-backdrop")
    suspend fun uploadMovieBackdrop(@Path("id") movieId: String, @Part image: MultipartBody.Part): Response<ApiResponse<Movie>>

    @GET("organizer/movies/cinemas")
    suspend fun ownerListCinemas(@QueryMap filters: Map<String, String> = emptyMap()): Response<PaginatedResponse<Cinema>>

    @GET("organizer/movies/cinemas/{id}")
    suspend fun ownerGetCinema(@Path("id") cinemaId: String): Response<ApiResponse<Cinema>>

    @POST("organizer/movies/cinemas")
    suspend fun ownerCreateCinema(@Body request: RequestBody): Response<ApiResponse<Cinema>>

    @PATCH("organizer/movies/cinemas/{id}")
    suspend fun ownerUpdateCinema(@Path("id") cinemaId: String, @Body request: RequestBody): Response<ApiResponse<Cinema>>

    @DELETE("organizer/movies/cinemas/{id}")
    suspend fun ownerDeleteCinema(@Path("id") cinemaId: String): Response<ApiResponse<Any>>

    @GET("organizer/movies/screens")
    suspend fun ownerListScreens(@QueryMap filters: Map<String, String> = emptyMap()): Response<PaginatedResponse<Screen>>

    @GET("organizer/movies/screens/{id}")
    suspend fun ownerGetScreen(@Path("id") screenId: String): Response<ApiResponse<Screen>>

    @POST("organizer/movies/cinemas/{cinemaId}/screens")
    suspend fun ownerCreateScreen(@Path("cinemaId") cinemaId: String, @Body request: RequestBody): Response<ApiResponse<Screen>>

    @PATCH("organizer/movies/screens/{id}")
    suspend fun ownerUpdateScreen(@Path("id") screenId: String, @Body request: RequestBody): Response<ApiResponse<Screen>>

    @DELETE("organizer/movies/screens/{id}")
    suspend fun ownerDeleteScreen(@Path("id") screenId: String): Response<ApiResponse<Any>>

    @GET("organizer/movies/showtimes")
    suspend fun ownerListShowtimes(@QueryMap filters: Map<String, String> = emptyMap()): Response<PaginatedResponse<Showtime>>

    @GET("organizer/movies/showtimes/{id}")
    suspend fun ownerGetShowtime(@Path("id") showtimeId: String): Response<ApiResponse<Showtime>>

    @POST("organizer/movies/showtimes")
    suspend fun ownerCreateShowtime(@Body request: RequestBody): Response<ApiResponse<Showtime>>

    @PATCH("organizer/movies/showtimes/{id}")
    suspend fun ownerUpdateShowtime(@Path("id") showtimeId: String, @Body request: RequestBody): Response<ApiResponse<Showtime>>

    @DELETE("organizer/movies/showtimes/{id}")
    suspend fun ownerDeleteShowtime(@Path("id") showtimeId: String): Response<ApiResponse<Any>>

    @GET("organizer/movies/price-caps")
    suspend fun ownerListPriceCaps(@QueryMap filters: Map<String, String> = emptyMap()): Response<PaginatedResponse<Any>>

    @GET("organizer/movies/price-caps/{id}")
    suspend fun ownerGetPriceCap(@Path("id") priceCapId: String): Response<ApiResponse<Any>>

    @POST("organizer/movies/price-caps")
    suspend fun ownerCreatePriceCap(@Body request: RequestBody): Response<ApiResponse<Any>>

    @PATCH("organizer/movies/price-caps/{id}")
    suspend fun ownerUpdatePriceCap(@Path("id") id: String, @Body request: RequestBody): Response<ApiResponse<Any>>

    @DELETE("organizer/movies/price-caps/{id}")
    suspend fun ownerDeletePriceCap(@Path("id") id: String): Response<ApiResponse<Any>>

    @GET("organizer/movies/screens/{screenId}/layout-versions")
    suspend fun ownerLayoutVersions(@Path("screenId") screenId: String): Response<ApiResponse<List<LayoutVersion>>>

    @GET("organizer/movies/screens/{screenId}/layout-versions/current")
    suspend fun ownerCurrentLayout(@Path("screenId") screenId: String): Response<ApiResponse<LayoutVersion>>

    @POST("organizer/movies/screens/{screenId}/layout-versions")
    suspend fun ownerCreateLayoutForScreen(@Path("screenId") screenId: String, @Body request: RequestBody): Response<ApiResponse<LayoutVersion>>

    @PATCH("organizer/movies/layout-versions/{id}/set-current")
    suspend fun ownerSetCurrentLayout(@Path("id") layoutId: String): Response<ApiResponse<LayoutVersion>>

    @GET("organizer/movies/layout-versions/{id}/seats")
    suspend fun ownerGetLayoutSeats(@Path("id") layoutId: String): Response<ApiResponse<List<Seat>>>

    @POST("organizer/movies/offline-bookings")
    suspend fun createOfflineBooking(@Body request: RequestBody): Response<ApiResponse<MovieBookingResponse>>

    @GET("organizer/movies/offline-bookings")
    suspend fun listOfflineBookings(@QueryMap filters: Map<String, String> = emptyMap()): Response<PaginatedResponse<Booking>>

    @GET("organizer/movies/offline-bookings/{id}")
    suspend fun getOfflineBooking(@Path("id") bookingId: String): Response<ApiResponse<Booking>>

    // ==================== OWNER DASHBOARD ====================
    @GET("owner/dashboard")
    suspend fun getDashboard(@QueryMap params: Map<String, String> = emptyMap()): Response<ApiResponse<Map<String, Any>>>

    @GET("owner/settlements")
    suspend fun getSettlements(@QueryMap filters: Map<String, String> = emptyMap()): Response<PaginatedResponse<Settlement>>

    @GET("owner/movies/analytics")
    suspend fun getMovieAnalytics(@QueryMap params: Map<String, String> = emptyMap()): Response<ApiResponse<Map<String, Any>>>

    @GET("owner/events/analytics")
    suspend fun getEventAnalytics(@QueryMap params: Map<String, String> = emptyMap()): Response<ApiResponse<Map<String, Any>>>

    // ==================== HEALTH ====================
    @GET(NetworkConstants.Endpoints.HEALTH_LIVE)
    suspend fun healthLive(): Response<ApiResponse<Any>>

    @GET(NetworkConstants.Endpoints.HEALTH_READY)
    suspend fun healthReady(): Response<ApiResponse<Any>>
}
