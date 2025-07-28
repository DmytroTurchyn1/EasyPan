package com.cook.easypan.easypan.data.repository

import com.cook.easypan.easypan.data.database.FirestoreClient
import com.cook.easypan.easypan.data.mappers.toUserData
import com.cook.easypan.easypan.domain.UserData
import com.cook.easypan.easypan.domain.UserRepository

class DefaultUserRepository(
    private val firestoreDataSource: FirestoreClient
) : UserRepository {

    override suspend fun getUserData(userId: String): UserData {
        return firestoreDataSource
            .getUserData(userId)
            .toUserData()
    }

}