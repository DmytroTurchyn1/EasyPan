package com.cook.easypan.easypan.data.repository

import android.util.Log
import com.cook.easypan.easypan.data.auth.GoogleAuthUiClient
import com.cook.easypan.easypan.data.database.FirestoreClient
import com.cook.easypan.easypan.data.mappers.toUserData
import com.cook.easypan.easypan.data.mappers.toUserDto
import com.cook.easypan.easypan.domain.AuthResponse
import com.cook.easypan.easypan.domain.User
import com.cook.easypan.easypan.domain.UserData
import com.cook.easypan.easypan.domain.UserRepository
import kotlinx.coroutines.flow.Flow

class DefaultUserRepository(
    private val firestoreDataSource: FirestoreClient,
    private val googleAuthClient: GoogleAuthUiClient
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
            Log.e("DefaultUserRepository", "Error updating user data: ${e.message}")
            AuthResponse.Failure(e.message ?: "Unknown error")
        }
    }

    override suspend fun getCurrentUser(): User? {
        val baseUser = googleAuthClient.getSignedInUser() ?: return null
        return try {
            val userData = getUserData(baseUser.userId)
            baseUser.copy(data = userData)
        } catch (e: Exception) {
            Log.w(
                "DefaultUserRepository",
                "Failed to fetch user data for ${baseUser.userId}: ${e.message}"
            )
            baseUser
        }
    }

    override fun signOut() = googleAuthClient.signOut()

    override fun isUserSignedIn(): Boolean {
        return googleAuthClient.getSignedInUser() != null
    }

    override suspend fun signInWithGoogle(): Flow<AuthResponse> = googleAuthClient.signIn()
}