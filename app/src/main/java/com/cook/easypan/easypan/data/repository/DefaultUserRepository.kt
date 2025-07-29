package com.cook.easypan.easypan.data.repository

import android.util.Log
import com.cook.easypan.easypan.data.database.FirestoreClient
import com.cook.easypan.easypan.data.mappers.toUserData
import com.cook.easypan.easypan.data.mappers.toUserDto
import com.cook.easypan.easypan.domain.AuthResponse
import com.cook.easypan.easypan.domain.UserData
import com.cook.easypan.easypan.domain.UserRepository

class DefaultUserRepository(
    private val firestoreDataSource: FirestoreClient,
) : UserRepository {

    override suspend fun getUserData(userId: String): UserData {
        return firestoreDataSource
            .getUserData(userId)
            .toUserData()
    }

    override suspend fun updateUserData(
        userId: String,
        userData: UserData
    ): AuthResponse {
        return try {
            firestoreDataSource.updateUserData(
                userId = userId,
                userData = userData
                    .toUserDto()
            )
            AuthResponse.Success
        } catch (e: Exception) {
            Log.e("FirestoreClient", "Error updating user data userRepo: ${e.message}")
            AuthResponse.Failure(e.message ?: "Unknown error")
        }
    }
}