/*
 * Created  25/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.data.database

import com.cook.easypan.core.util.AnalyticsEvent
import com.cook.easypan.core.util.AnalyticsParam
import com.cook.easypan.core.util.FAVORITE_COLLECTION
import com.cook.easypan.core.util.RECIPES_COLLECTION
import com.cook.easypan.core.util.USER_DATA_COLLECTION
import com.cook.easypan.easypan.data.analytics.AnalyticsClient
import com.cook.easypan.easypan.data.dto.RecipeDto
import com.cook.easypan.easypan.data.dto.UserDto
import com.cook.easypan.easypan.data.mappers.toRecipeDto
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirestoreClient(
    private val firestore: FirebaseFirestore,
    private val analytics: AnalyticsClient
) {
    private suspend fun documentExists(documentRef: DocumentReference): Boolean {
        val snapshot = documentRef.get().await()
        return snapshot.exists()
    }

    private suspend fun getCollection(collectionName: String): List<DocumentSnapshot> {
        return firestore.collection(collectionName)
            .get()
            .await()
            .documents
    }

    suspend fun getRecipes(): List<RecipeDto> {
        return getCollection(RECIPES_COLLECTION)
            .mapNotNull { document -> document.toRecipeDto() }
    }

    suspend fun getUserData(userId: String): UserDto {
        val userRef = firestore.collection(USER_DATA_COLLECTION).document(userId)
        return firestore.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            if (!snapshot.exists()) {
                analytics.track(
                    AnalyticsEvent.SIGN_UP,
                    mapOf(AnalyticsParam.USER_ID to userId)
                )
                val newUser = UserDto(0)
                transaction.set(userRef, newUser, SetOptions.merge())
                newUser
            } else {
                snapshot.toObject(UserDto::class.java) ?: UserDto(0)
            }
        }.await()

    }

    suspend fun incrementCookedRecipes(userId: String) {
        firestore.collection(USER_DATA_COLLECTION)
            .document(userId)
            .update("recipesCooked", FieldValue.increment(1))
            .await()
    }

    suspend fun getFavoriteRecipes(userId: String): List<RecipeDto> {
        return firestore.collection(USER_DATA_COLLECTION)
            .document(userId)
            .collection(FAVORITE_COLLECTION)
            .get()
            .await()
            .documents
            .mapNotNull { document -> document.toRecipeDto() }
    }

    suspend fun isRecipeFavorite(userId: String, recipeId: String): Boolean {
        val favoriteDocumentRef = firestore.collection(USER_DATA_COLLECTION)
            .document(userId)
            .collection(FAVORITE_COLLECTION)
            .document(recipeId)
        return documentExists(favoriteDocumentRef)
    }

    suspend fun addRecipeToFavorite(
        userId: String,
        recipe: RecipeDto
    ) {
        firestore.collection(USER_DATA_COLLECTION)
            .document(userId)
            .collection(FAVORITE_COLLECTION)
            .document(recipe.id)
            .set(recipe)
            .await()
    }

    suspend fun deleteRecipeFromFavorite(
        userId: String,
        recipeId: String
    ) {
        firestore.collection(USER_DATA_COLLECTION)
            .document(userId)
            .collection(FAVORITE_COLLECTION)
            .document(recipeId)
            .delete()
            .await()
    }

    suspend fun deleteUserData(userId: String) {
        val userRef = firestore.collection(USER_DATA_COLLECTION).document(userId)
        val favoriteDocuments = userRef
            .collection(FAVORITE_COLLECTION)
            .get()
            .await()
            .documents
        val batch = firestore.batch()
        favoriteDocuments.forEach { document ->
            batch.delete(document.reference)
        }
        batch.delete(userRef)
        batch.commit().await()
    }
}
