package com.cook.easypan.easypan.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    @SerialName("recipesCooked")
    val recipesCooked: Int = 0
)