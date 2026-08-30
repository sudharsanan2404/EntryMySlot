# Android Backend Integration Plan

This plan aims to fix the existing errors in the Android app's data layer and complete the integration with the AWS backend server (98.130.20.52).

## User Review Required

> [!IMPORTANT]
> The `BASE_URL` in `RetrofitClient.kt` is currently set to `http://98.130.20.52:4000/api/v1/`. I will verify if this is reachable and correct during the implementation.

## Proposed Changes

### 1. Fix Authentication Layer
- Correct imports in `AuthApi.kt` and `AuthRepository.kt`.
- Ensure all models in `AuthModels.kt` match the backend expectations.

### 2. Fix Event Data Layer
- Fix imports in `EventApi.kt` and `EventRepository.kt`.
- Relocate or link models like `EventDto`, `EventDetailResponse`, and `EventStats` from `com.entrymyslot.app.data.model`.
- Remove redundant booking endpoints from `EventApi.kt` if they are handled by `BookingApi.kt`.

### 3. Fix Booking Data Layer
- Fix imports in `BookingApi.kt` and `BookingRepository.kt`.
- Ensure `BookingApi.kt` correctly maps to `/api/v1/bookings`.

### 4. Complete Movie and Turf APIs
- Synchronize `MovieApi.kt` and `TurfApi.kt` with the latest backend routes.
- Fix missing models in `Movie.kt` and `Turf.kt` (under `com.entrymyslot.app.data.model`).

### 5. Network & DI Cleanup
- Ensure `RetrofitClient` is correctly initialized and used across the app.
- Verify that `AuthInterceptor` correctly attaches the Bearer token.

## Verification Plan

### Automated Tests
- I will run `gradle assembleDebug` to ensure all compile errors are resolved.
- If possible, I will create small unit tests for Repositories to verify data parsing.

### Manual Verification
- Deploy the app to a device and check the logs (Logcat) for successful API responses from the AWS server.
