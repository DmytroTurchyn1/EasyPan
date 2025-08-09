package com.cook.easypan.core.domain

interface AuthResponse {
    data object Success : AuthResponse
    data class Failure(val error: String) : AuthResponse
}