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
import com.cook.easypan.core.domain.AppError
import com.cook.easypan.core.domain.Result
import com.cook.easypan.core.util.FAVORITES_CACHE_TIMEOUT
import com.cook.easypan.core.util.USER_DATA_CACHE_TIMEOUT
import com.cook.easypan.easypan.data.auth.AuthClient
import com.cook.easypan.easypan.data.database.FirestoreClient
import com.cook.easypan.easypan.data.datastore.AppSettings
import com.cook.easypan.easypan.data.mappers.toRecipe
import com.cook.easypan.easypan.data.mappers.toRecipeDto
import com.cook.easypan.easypan.data.mappers.toUser
import com.cook.easypan.easypan.data.mappers.toUserData
import com.cook.easypan.easypan.data.mappers.toUserDto
import com.cook.easypan.easypan.domain.model.Recipe
import com.cook.easypan.easypan.domain.model.User
import com.cook.easypan.easypan.domain.model.UserData
import com.cook.easypan.easypan.domain.repository.BillingRepository
import com.cook.easypan.easypan.domain.repository.UserRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DefaultUserRepository(
    private val firestoreDataSource: FirestoreClient,
    private val googleAuthClient: AuthClient,
    private val billingRepository: BillingRepository,
    private val context: Context
) : UserRepository {

    companion object {
        private const val TAG = "DefaultUserRepository"
    }

    override suspend fun getUserData(userId: String): UserData {
        return firestoreDataSource
            .getUserData(userId)
            .toUserData()
    }

    override suspend fun updateUserData(): Result {
        val userId = googleAuthClient.getSignedInUser()?.userId
            ?: return Result.Failure(AppError.NOT_SIGNED_IN)
        return try {
            firestoreDataSource.incrementCookedRecipes(userId = userId)

            context.dataStore.updateData { appSettings ->
                appSettings.copy(
                    cachedUserData = appSettings.cachedUserData?.let {
                        it.copy(recipesCooked = it.recipesCooked + 1)
                    }
                )
            }
            Result.Success
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update user data", e)
            Result.Failure(AppError.UNKNOWN)
        }
    }

    override suspend fun getFavoriteRecipes(): List<Recipe> {
        val userId = googleAuthClient.getSignedInUser()?.userId
            ?: throw IllegalStateException("User not logged in")
        val settings = context.dataStore.data.first()
        val cacheIsFresh =
            System.currentTimeMillis() - settings.lastCacheTimeFavorites < FAVORITES_CACHE_TIMEOUT
        // The cache is only valid for the account it was written for.
        if (settings.userId == userId && cacheIsFresh && settings.cacheFavoriteRecipes.isNotEmpty()) {
            return settings.cacheFavoriteRecipes.map { it.toRecipe() }
        }
        val favoriteList = firestoreDataSource.getFavoriteRecipes(userId)
        context.dataStore.updateData { appSettings ->
            appSettings.copy(
                userId = userId,
                cacheFavoriteRecipes = favoriteList,
                lastCacheTimeFavorites = System.currentTimeMillis()
            )
        }
        return favoriteList.map { it.toRecipe() }
    }

    override suspend fun addRecipeToFavorites(recipe: Recipe): Result {
        val userId = googleAuthClient.getSignedInUser()?.userId
            ?: return Result.Failure(AppError.NOT_SIGNED_IN)
        return try {
            val recipeDto = recipe.toRecipeDto()
            firestoreDataSource.addRecipeToFavorite(
                userId = userId,
                recipe = recipeDto
            )
            context.dataStore.updateData { appSettings ->
                appSettings.copy(
                    userId = userId,
                    cacheFavoriteRecipes = appSettings.cacheFavoriteRecipes
                        .filterNot { it.id == recipeDto.id } + recipeDto,
                    lastCacheTimeFavorites = System.currentTimeMillis()
                )
            }
            Result.Success
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add recipe to favorites", e)
            Result.Failure(AppError.UNKNOWN)
        }
    }

    override suspend fun deleteRecipeFromFavorites(recipeId: String): Result {
        val userId = googleAuthClient.getSignedInUser()?.userId
            ?: return Result.Failure(AppError.NOT_SIGNED_IN)
        return try {
            firestoreDataSource.deleteRecipeFromFavorite(
                userId = userId,
                recipeId = recipeId
            )
            context.dataStore.updateData { appSettings ->
                appSettings.copy(
                    cacheFavoriteRecipes = appSettings.cacheFavoriteRecipes.filterNot { it.id == recipeId },
                    lastCacheTimeFavorites = System.currentTimeMillis()
                )
            }
            Result.Success
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete recipe from favorites", e)
            Result.Failure(AppError.UNKNOWN)
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
        val settings = context.dataStore.data.first()
        val currentTime = System.currentTimeMillis()
        if (currentTime - settings.lastCacheTimeUserData < USER_DATA_CACHE_TIMEOUT) {
            val cachedUser = settings.toUser()
            if (cachedUser.userId == baseUser.userId) {
                return cachedUser
            }
        }

        return try {
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
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch user data", e)
            baseUser
        }
    }

    override suspend fun signOut() {
        googleAuthClient.signOut()
        billingRepository.logOut()
        clearLocalCache()
    }

    override suspend fun deleteAccount(activityContext: Context): Result {
        val userId = googleAuthClient.getSignedInUser()?.userId
            ?: return Result.Failure(AppError.NOT_SIGNED_IN)
        return try {
            // Delete Firestore data first: it requires an authenticated user.
            firestoreDataSource.deleteUserData(userId)
            when (val result = googleAuthClient.deleteAccount(activityContext)) {
                is Result.Success -> {
                    // Drop the personal data while RevenueCat still points at the real customer;
                    // logOut below switches it to a fresh anonymous id.
                    billingRepository.setUserAttributes(email = null, displayName = null)
                    billingRepository.logOut()
                    clearLocalCache()
                    Result.Success
                }

                is Result.Failure -> result
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete account data", e)
            Result.Failure(AppError.UNKNOWN)
        }
    }

    override fun isUserSignedIn(): Boolean = googleAuthClient.getSignedInUser() != null

    override suspend fun signInWithGoogle(activityContext: Context): Result {
        val result = googleAuthClient.signInWithGoogle(activityContext)
        if (result is Result.Success) {
            // Tie billing identity to the account so purchases follow the user across devices.
            // logIn never blocks auth: DefaultBillingRepository logs failures internally.
            googleAuthClient.getSignedInUser()?.let { user ->
                billingRepository.logIn(user.userId)
                // After logIn: attributes bind to whichever customer is current when set.
                billingRepository.setUserAttributes(
                    email = user.email,
                    displayName = user.username
                )
            }
        }
        return result
    }

    private suspend fun clearLocalCache() {
        context.dataStore.updateData { AppSettings() }
    }
}
