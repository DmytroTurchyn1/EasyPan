/*
 * Created  25/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.data.mappers

import com.cook.easypan.easypan.data.datastore.AppSettings
import com.cook.easypan.easypan.data.dto.UserDto
import com.cook.easypan.easypan.domain.model.User
import com.cook.easypan.easypan.domain.model.UserData

fun UserDto.toUserData(): UserData {
    return UserData(
        recipesCooked = recipesCooked
    )
}
fun UserData.toUserDto(): UserDto {
    return UserDto(
        recipesCooked = recipesCooked
    )
}

fun AppSettings.toUser(): User {
    return User(
        username = userName,
        profilePictureUrl = userPhotoUrl,
        data = cachedUserData?.toUserData()
    )
}