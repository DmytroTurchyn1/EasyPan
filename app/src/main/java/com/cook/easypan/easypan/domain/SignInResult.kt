package com.cook.easypan.easypan.domain

interface AuthResponse {
    data object Success : AuthResponse
    data class Failure(val error: String) : AuthResponse
}