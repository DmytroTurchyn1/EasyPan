package com.cook.easypan.easypan.data.repository

import android.content.Context
import android.util.Log
import com.cook.easypan.core.domain.AuthResponse
import com.cook.easypan.easypan.data.auth.AuthClient
import com.cook.easypan.easypan.data.database.FirestoreClient
import com.cook.easypan.easypan.data.mappers.toUserData
import com.cook.easypan.easypan.data.mappers.toUserDto
import com.cook.easypan.easypan.domain.model.User
import com.cook.easypan.easypan.domain.model.UserData
import com.cook.easypan.easypan.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class DefaultUserRepository(
    private val firestoreDataSource: FirestoreClient,
    private val googleAuthClient: AuthClient
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
            AuthResponse.Failure(e.message ?: "Unknown error")
        }
    }

    override suspend fun getCurrentUser(): User? {
        val baseUser = googleAuthClient.getSignedInUser() ?: return null
        return try {
            val userData = getUserData(baseUser.userId)
            baseUser.copy(data = userData)
        } catch (e: Exception) {
            Log.e(
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

    override suspend fun signInWithGoogle(activityContext: Context): Flow<AuthResponse> =
        googleAuthClient.signInWithGoogle(activityContext)
}