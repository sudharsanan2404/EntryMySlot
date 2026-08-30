# Implementation Plan: Location Dropdown Enhancement

Enhance the UI for the Location dropdown in the `AuthScreen` registration form and integrate location services (permissions and user location detection).

## User Review Required

> [!IMPORTANT]
> The location selection will be added to the registration form. User will be able to either pick a location manually or use "Detect my location".

## Proposed Changes

### Core Utilities

#### [NEW] [LocationHelper.kt](file:///C:/Users/navan/AndroidStudioProjects/EntryMySlot/app/src/main/java/com/entrymyslot/app/core/utils/LocationHelper.kt)
Extract location-related logic (permissions, GPS check, fetching current city) from `HomeViewModel.kt` to a shared utility class.

### Authentication Module

#### [MODIFY] [AuthViewModel.kt](file:///C:/Users/navan/AndroidStudioProjects/EntryMySlot/app/src/main/java/com/entrymyslot/app/screens/auth/AuthViewModel.kt)
- Update `AuthUiState` to include a `location` field.
- Add `updateLocation` function to the ViewModel.
- Update `register` to include the city (optional, depending on backend support).

#### [MODIFY] [AuthScreen.kt](file:///C:/Users/navan/AndroidStudioProjects/EntryMySlot/app/src/main/java/com/entrymyslot/app/screens/auth/AuthScreen.kt)
- Add a themed `LocationPickerField` to the registration form.
- Implement a themed `LocationDropdown` or a Dialog similar to `ProfessionalLocationPicker` but styled specifically for the Auth screen.
- Integrate `rememberLocationFetcher` to handle permissions and location detection.

### Home Module

#### [MODIFY] [HomeViewModel.kt](file:///C:/Users/navan/AndroidStudioProjects/EntryMySlot/app/src/main/java/com/entrymyslot/app/screens/home/HomeViewModel.kt)
- Refactor to use the new `LocationHelper.kt` utility.

## Verification Plan

### Automated Tests
- N/A (Manual verification on device is preferred for location services).

### Manual Verification
1.  Deploy the app to an emulator or physical device.
2.  Navigate to the Registration tab in `AuthScreen`.
3.  Verify the new Location field is present.
4.  Click "Detect my location" and verify permission request appears.
5.  After granting permission, verify the city is correctly detected and populated.
6.  Verify the dropdown UI matches the app theme (EntryBlue and EntryOrange).
