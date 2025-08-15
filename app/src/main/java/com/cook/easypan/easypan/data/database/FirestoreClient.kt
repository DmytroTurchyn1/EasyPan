/*
 * Created  15/8/2025
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirestoreClient(
    private val firestore: FirebaseFirestore
) {
    private suspend fun documentExists(documentRef: DocumentReference): Boolean {
        val snap = documentRef.get().await()
        return snap.exists()
    }

    private suspend fun getCollection(collectionName: String): List<DocumentSnapshot> {
        return try {
            firestore.collection(collectionName)
                .get()
                .await()
                .documents
        } catch (e: Exception) {
            throw e
        }
    }

    private suspend fun addDocument(
        documentId: String,
        collectionName: String,
        data: UserDto
    ) {
        try {
            firestore.collection(collectionName)
                .document(documentId)
                .set(data, SetOptions.merge())
                .await()

        } catch (e: Exception) {
            throw e
        }
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
        val user = firestore.collection(USER_DATA_COLLECTION).document(userId)
        return try {
            user
                .get()
                .await()
                .toObject(UserDto::class.java) ?: UserDto(0)

        } catch (e: Exception) {
            Log.e("FirestoreClient", "Error getting user data: ${e.message}")
            addDocument(
                documentId = userId,
                collectionName = USER_DATA_COLLECTION,
                data = UserDto(0)
            )
            return UserDto(0)
        }
    }

    suspend fun updateUserData(
        userId: String,
        userData: UserDto
    ) {
        return try {
            addDocument(
                documentId = userId,
                collectionName = USER_DATA_COLLECTION,
                data = userData
            )
        } catch (e: Exception) {
            Log.e("FirestoreClient", "Error updating user data: ${e.message}")
            throw e
        }
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