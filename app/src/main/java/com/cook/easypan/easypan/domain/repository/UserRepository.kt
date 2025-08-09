package com.cook.easypan.easypan.domain.repository

import android.content.Context
import com.cook.easypan.core.domain.AuthResponse
import com.cook.easypan.easypan.domain.model.User
import com.cook.easypan.easypan.domain.model.UserData
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getUserData(userId: String): UserData
    suspend fun updateUserData(userId: String, userData: UserData): AuthResponse
    suspend fun getCurrentUser(): User?
    suspend fun signInWithGoogle(activityContext: Context): Flow<AuthResponse>
    fun isUserSignedIn(): Boolean
    fun signOut()
}