package com.cook.easypan.easypan.domain

interface UserRepository {
    suspend fun getUserData(userId: String): UserData
}