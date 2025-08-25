/*
 * Created  25/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.data.database

import android.util.Log
import com.cook.easypan.core.util.FAVORITE_COLLECTION
import com.cook.easypan.core.util.RECIPES_COLLECTION
import com.cook.easypan.core.util.USER_DATA_COLLECTION
import com.cook.easypan.easypan.data.dto.RecipeDto
import com.cook.easypan.easypan.data.dto.UserDto
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirestoreClient(
    private val firestore: FirebaseFirestore
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
        return try {
            getCollection(RECIPES_COLLECTION)
                .mapNotNull { document ->
                    document.toObject(RecipeDto::class.java)?.copy(id = document.id)
                }
        } catch (e: Exception) {
            throw e
        }

    }

    suspend fun getUserData(userId: String): UserDto {
        val userRef = firestore.collection(USER_DATA_COLLECTION).document(userId)
        return firestore.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            if (!snapshot.exists()) {
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

        val favoriteRecipeCollection = firestore.collection(USER_DATA_COLLECTION)
            .document(userId)
            .collection(FAVORITE_COLLECTION)

        return try {
            favoriteRecipeCollection
                .get()
                .await()
                .documents
                .mapNotNull { document ->
                    document.toObject(RecipeDto::class.java)?.copy(id = document.id)
                }
        } catch (e: Exception) {
            Log.e("FirestoreClient", "Error getting favorite recipes: ${e.message}")
            emptyList()
        }
    }

    suspend fun isRecipeFavorite(userId: String, recipeId: String): Boolean {
        val favoriteDocumentRef = firestore.collection(USER_DATA_COLLECTION)
            .document(userId)
            .collection(FAVORITE_COLLECTION)
            .document(recipeId)
        return try {
            documentExists(favoriteDocumentRef)
        } catch (e: Exception) {
            Log.e("FirestoreClient", "Error checking if recipe is favorite: ${e.message}")
            false
        }
    }

    suspend fun addRecipeToFavorite(
        userId: String,
        recipe: RecipeDto
    ) {
        try {
            val favoriteCollection = firestore.collection(USER_DATA_COLLECTION)
                .document(userId)
                .collection(FAVORITE_COLLECTION)

            favoriteCollection
                .document(recipe.id)
                .set(recipe)
                .await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun deleteRecipeFromFavorite(
        userId: String,
        recipeId: String
    ): Boolean {
        val docRef = firestore.collection(USER_DATA_COLLECTION)
            .document(userId)
            .collection(FAVORITE_COLLECTION)
            .document(recipeId)

        return try {
            docRef
                .delete()
                .await()
            true
        } catch (e: Exception) {
            Log.e("FirestoreClient", "Error deleting recipe from favorites: ${e.message}")
            false
        }
    }

}