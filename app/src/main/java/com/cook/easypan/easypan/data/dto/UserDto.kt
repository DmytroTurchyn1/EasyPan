package com.cook.easypan.easypan.data.dto

import com.google.firebase.firestore.PropertyName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    @PropertyName("recipesCooked") val recipesCooked: Int = 0
)