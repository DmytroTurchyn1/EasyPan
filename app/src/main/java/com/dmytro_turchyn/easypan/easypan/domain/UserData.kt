package com.dmytro_turchyn.easypan.easypan.domain

data class UserData(
    val userId : String,
    val username: String?,
    val profilePictureUrl: String?
)

data class SignInResult(
    val data: UserData?,
    val errorMessage: String?
)
