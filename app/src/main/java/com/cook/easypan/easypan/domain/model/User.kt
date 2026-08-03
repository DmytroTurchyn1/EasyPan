package com.cook.easypan.easypan.domain.model

data class User(
    val userId: String = "",
    val username: String? = null,
    val profilePictureUrl: String? = null,
    /**
     * Only populated by `AuthClient.getSignedInUser` from the live Firebase session. Null on a
     * [User] rebuilt from the local cache — AppSettings does not persist it.
     */
    val email: String? = null,
    val data: UserData? = null
)