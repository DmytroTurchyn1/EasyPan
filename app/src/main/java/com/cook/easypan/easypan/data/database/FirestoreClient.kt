/*
 * Created  13/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

package com.cook.easypan.easypan.data.database

import com.cook.easypan.core.util.FAVORITE_COLLECTION
import com.cook.easypan.core.util.RECIPES_COLLECTION
import com.cook.easypan.core.util.USER_DATA_COLLECTION
import com.cook.easypan.easypan.data.dto.RecipeDto
import com.cook.easypan.easypan.data.dto.UserDto
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreClient(
    private val firestore: FirebaseFirestore
) {

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
    private suspend fun collectionExists(collectionRef: CollectionReference): Boolean {
        val snap = collectionRef.get().await()
        return !snap.isEmpty
    }

    private suspend fun addDocument(
        documentId: String,
        collectionName: String,
        data: UserDto
    ) {
        try {
            firestore.collection(collectionName)
                .document(documentId)
                .set(data)
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
        return try {
            val user = firestore.collection(USER_DATA_COLLECTION)
                .document(userId)
                .get()
                .await()
            if (!user.exists()) {
                addDocument(
                    documentId = userId,
                    collectionName = USER_DATA_COLLECTION,
                    data = UserDto(0)
                )
                return UserDto(0)
            } else {
                user.toObject(UserDto::class.java) ?: UserDto(0)
            }
        } catch (e: Exception) {
            throw e
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
            throw e
        }
    }

    suspend fun getFavoriteRecipes(userId: String): List<RecipeDto> {
        return try {
            if (collectionExists(
                    firestore.collection(USER_DATA_COLLECTION).document(userId)
                        .collection(FAVORITE_COLLECTION)
                )
            ) {
                firestore.collection(USER_DATA_COLLECTION)
                    .document(userId)
                    .collection(FAVORITE_COLLECTION)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { document ->
                        document.toObject(RecipeDto::class.java)?.copy(id = document.id)
                    }
            } else emptyList()

        } catch (e: Exception) {
            throw e
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
            // Check if the document exists before attempting to delete
            val snap = docRef.get().await()

            if (snap.exists()) {
                docRef
                    .delete()
                    .await()
                true
            } else {

                false
            }
        } catch (e: Exception) {
            throw e
        }
    }

}