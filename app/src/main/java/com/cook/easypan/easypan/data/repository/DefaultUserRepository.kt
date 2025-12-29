/*
 * Created  26/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.data.repository

import android.content.Context
import android.util.Log
import com.cook.easypan.app.dataStore
import com.cook.easypan.core.domain.Result
import com.cook.easypan.core.util.FAVORITES_CACHE_TIMEOUT
import com.cook.easypan.core.util.USER_DATA_CACHE_TIMEOUT
import com.cook.easypan.easypan.data.auth.AuthClient
import com.cook.easypan.easypan.data.database.FirestoreClient
import com.cook.easypan.easypan.data.mappers.toRecipe
import com.cook.easypan.easypan.data.mappers.toRecipeDto
import com.cook.easypan.easypan.data.mappers.toUser
import com.cook.easypan.easypan.data.mappers.toUserData
import com.cook.easypan.easypan.data.mappers.toUserDto
import com.cook.easypan.easypan.domain.model.Recipe
import com.cook.easypan.easypan.domain.model.User
import com.cook.easypan.easypan.domain.model.UserData
import com.cook.easypan.easypan.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DefaultUserRepository(
    private val firestoreDataSource: FirestoreClient,
    private val googleAuthClient: AuthClient,
    private val context: Context
) : UserRepository {
    override suspend fun getUserData(userId: String): UserData {
        return firestoreDataSource
            .getUserData(userId)
            .toUserData()
    }

    override suspend fun updateUserData(): Result {
        val userId = googleAuthClient.getSignedInUser()?.userId
            ?: throw IllegalStateException("User not logged in")
        return try {
            firestoreDataSource.incrementCookedRecipes(userId = userId)

            val currentSettings = context.dataStore.data.first()
            val updatedUserData = currentSettings.cachedUserData?.let {
                it.copy(
                    recipesCooked = it.recipesCooked + 1//TODO: check if it works
            )
            }


            context.dataStore.updateData { appSettings ->
                appSettings.copy(
                    cachedUserData = updatedUserData
                )
            }
            Result.Success
        } catch (e: Exception) {
            Result.Failure(e.message ?: "Unknown error")
        }

    }

    override suspend fun getFavoriteRecipes(): List<Recipe> {
        val userId = googleAuthClient.getSignedInUser()?.userId
            ?: throw IllegalStateException("User not logged in")
        val lastCacheTimeFavorites = context.dataStore.data.first().lastCacheTimeFavorites
        if (System.currentTimeMillis() - lastCacheTimeFavorites < FAVORITES_CACHE_TIMEOUT) {
            val cachedRecipes = context.dataStore.data.first().cacheFavoriteRecipes
            if (cachedRecipes.isNotEmpty()) {
                Log.d("DefaultUserRepository", "Returning recently cached favorite recipes")
                return cachedRecipes.map { it.toRecipe() }
            }
        }
        return try {
            val favoriteList = firestoreDataSource.getFavoriteRecipes(userId)
            context.dataStore.updateData { appSettings ->
                appSettings.copy(
                    cacheFavoriteRecipes = favoriteList,
                    lastCacheTimeFavorites = System.currentTimeMillis()
                )
            }
            Log.d("DefaultUserRepository", "Caching favorite recipes")
            favoriteList.map { it.toRecipe() }
        } catch (e: Exception) {
            throw e
        }

    }

    override suspend fun addRecipeToFavorites(
        recipe: Recipe
    ): Result {
        val userId = googleAuthClient.getSignedInUser()?.userId
            ?: throw IllegalStateException("User not logged in")
        return try {
            firestoreDataSource.addRecipeToFavorite(
                userId = userId,
                recipe = recipe.toRecipeDto()
            )
            context.dataStore.updateData { appSettings ->
                appSettings.copy(
                    cacheFavoriteRecipes = appSettings.cacheFavoriteRecipes + recipe.toRecipeDto(),
                    lastCacheTimeFavorites = System.currentTimeMillis()
                )
            }
            Result.Success
        } catch (e: Exception) {
            Result.Failure(e.message ?: "Unknown error")
        }

    }

    override suspend fun deleteRecipeFromFavorites(recipeId: String): Result {
        return try {
            val userId = googleAuthClient.getSignedInUser()?.userId
                ?: throw IllegalStateException("User not logged in")
            val deleteFavoriteRecipe = firestoreDataSource.deleteRecipeFromFavorite(
                userId = userId,
                recipeId = recipeId
            )
            if (deleteFavoriteRecipe) {
                context.dataStore.updateData { appSettings ->
                    appSettings.copy(
                        cacheFavoriteRecipes = appSettings.cacheFavoriteRecipes.filterNot { it.id == recipeId },
                        lastCacheTimeFavorites = System.currentTimeMillis()
                    )
                }
                Result.Success
            } else {
                Result.Failure("Failed to delete recipe from favorites")
            }


        } catch (e: Exception) {
            Result.Failure(e.message ?: "Unknown error")
        }

    }

    override suspend fun isRecipeFavorite(recipeId: String): Boolean {
        val userId = googleAuthClient.getSignedInUser()?.userId
            ?: throw IllegalStateException("User not logged in")
        return firestoreDataSource.isRecipeFavorite(userId = userId, recipeId = recipeId)

    }

    override suspend fun updateKeepScreenOnDataStore(value: Boolean): Boolean {

        context.dataStore.updateData { settings ->
            settings.copy(
                keepScreenOn = value
            )
        }

        return value
    }

    override fun getKeepScreenOnDataStore(): Flow<Boolean> =
        context.dataStore.data.map { it.keepScreenOn }

    override suspend fun getCurrentUser(): User? {
        val baseUser = googleAuthClient.getSignedInUser() ?: return null
        val lastCacheTimeUserData = context.dataStore.data.first().lastCacheTimeUserData
        val currentTime = System.currentTimeMillis()
        if (System.currentTimeMillis() - lastCacheTimeUserData < USER_DATA_CACHE_TIMEOUT) {
            val cachedUser = context.dataStore.data.first().toUser()
            if (cachedUser.userId == baseUser.userId) {
                Log.d("DefaultUserRepository", "Returning recently cached user: $cachedUser")
                return cachedUser
            }
        }

        return try {
            Log.d("DefaultUserRepository", "Returning fetched user")
            val userData = getUserData(baseUser.userId)
            val userWithData = baseUser.copy(data = userData)
            context.dataStore.updateData { appSettings ->
                appSettings.copy(
                    userId = userWithData.userId,
                    userName = userWithData.username,
                    userPhotoUrl = userWithData.profilePictureUrl,
                    cachedUserData = userData.toUserDto(),
                    lastCacheTimeUserData = currentTime
                )
            }
            userWithData
        } catch (e: Exception) {
            Log.e(
                "DefaultUserRepository",
                "Failed to fetch user data for ${baseUser.userId}: ${e.message}"
            )
            baseUser
        }

    }


    override fun signOut() = googleAuthClient.signOut()

    override fun isUserSignedIn(): Boolean = googleAuthClient.getSignedInUser() != null


    override fun signInWithGoogle(activityContext: Context): Flow<Result> =
        googleAuthClient.signInWithGoogle(activityContext)
}