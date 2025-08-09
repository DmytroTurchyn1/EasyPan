package com.cook.easypan.easypan.domain.model

data class User(
    val userId: String = "",
    val username: String? = null,
    val profilePictureUrl: String? = null,
    val data: UserData? = null
)