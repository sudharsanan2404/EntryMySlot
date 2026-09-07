package com.entrymyslotbe.app.network.model

data class Organization(
    val id: String? = null,
    val name: String? = null,
    val type: String? = null,       // turf, events, movies, concerts, other
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val logo: String? = null,
    val description: String? = null,
    val isActive: Boolean = true,
    val isVerified: Boolean = false,
    val ownerId: String? = null,
    val createdAt: String? = null
)

data class Organizer(
    val id: String? = null,
    val email: String? = null,
    val name: String? = null,
    val phone: String? = null,
    val role: String? = null,  // owner, manager
    val organizationId: String? = null,
    val organizationName: String? = null,
    val isActive: Boolean = true,
    val permissions: Map<String, Boolean>? = null,
    val createdAt: String? = null
)

data class OrganizerLoginRequest(
    val email: String,
    val password: String
)

data class OrganizerLoginResponse(
    val organizer: Organizer? = null,
    val user: Organizer? = null,
    val accessToken: String? = null,
    val refreshToken: String? = null
) {
    fun resolvedOrganizer(): Organizer? = organizer ?: user
}

data class OrganizerApplication(
    val id: String? = null,
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val organizationName: String? = null,
    val listingCategory: String? = null,
    val status: String? = null,  // pending, approved, rejected
    val createdAt: String? = null
)

data class Manager(
    val id: String? = null,
    val email: String? = null,
    val name: String? = null,
    val phone: String? = null,
    val role: String? = null,
    val organizationId: String? = null,
    val isActive: Boolean = true,
    val isDisabled: Boolean = false,
    val permissions: Map<String, Boolean>? = null,
    val assignedVenueIds: List<String>? = null,
    val createdAt: String? = null
)

data class CreateManagerRequest(
    val email: String,
    val name: String,
    val password: String,
    val phone: String? = null,
    val permissions: Map<String, Boolean>? = null,
    val assignedVenueIds: List<String>? = null
)

data class UpdateManagerRequest(
    val name: String? = null,
    val phone: String? = null,
    val permissions: Map<String, Boolean>? = null,
    val assignedVenueIds: List<String>? = null
)

data class Invitation(
    val id: String? = null,
    val email: String? = null,
    val token: String? = null,
    val role: String? = null,
    val organizationId: String? = null,
    val organizationName: String? = null,
    val status: String? = null,  // pending, accepted, expired
    val expiresAt: String? = null,
    val createdAt: String? = null
)

data class CreateInvitationRequest(
    val email: String,
    val role: String
)

data class BankingInfo(
    val accountHolderName: String? = null,
    val accountNumber: String? = null,
    val ifscCode: String? = null,
    val bankName: String? = null,
    val branchName: String? = null,
    val upiId: String? = null
)
