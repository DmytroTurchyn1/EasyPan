package com.cook.easypan.easypan.data.mappers

import com.cook.easypan.easypan.data.dto.UserDto
import com.cook.easypan.easypan.domain.UserData

fun UserDto.toUserData(): UserData {
    return UserData(
        recipesCooked = recipesCooked
    )
}