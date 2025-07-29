package com.cook.easypan.easypan.domain

interface UserRepository {
    suspend fun getUserData(userId: String): UserData
    suspend fun updateUserData(userId: String, userData: UserData): AuthResponse
}