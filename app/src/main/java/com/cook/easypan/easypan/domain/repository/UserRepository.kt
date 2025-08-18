/*
 * Created  18/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.domain.repository

import android.content.Context
import com.cook.easypan.core.domain.Result
import com.cook.easypan.easypan.domain.model.Recipe
import com.cook.easypan.easypan.domain.model.User
import com.cook.easypan.easypan.domain.model.UserData
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getUserData(userId: String): UserData
    suspend fun updateUserData(userData: UserData): Result
    suspend fun getFavoriteRecipes(): List<Recipe>
    suspend fun addRecipeToFavorites(recipe: Recipe): Result
    suspend fun deleteRecipeFromFavorites(recipeId: String): Result
    suspend fun isRecipeFavorite(recipeId: String): Boolean
    suspend fun updateKeepScreenOnDataStore(value: Boolean): Boolean
    suspend fun getCurrentUser(): User?
    fun getKeepScreenOnDataStore(): Flow<Boolean>
    fun signInWithGoogle(activityContext: Context): Flow<Result>
    fun isUserSignedIn(): Boolean
    fun signOut()
}