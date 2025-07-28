package com.cook.easypan.easypan.data.database

import com.cook.easypan.easypan.data.dto.RecipeDto
import com.cook.easypan.easypan.data.dto.UserDto
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class FirestoreClient {
    private val firestore = Firebase.firestore
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
                .set(data)
                .await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getRecipes(): List<RecipeDto> {
        return try {
            getCollection("Recipes")
                .mapNotNull { document ->
                    document.toObject(RecipeDto::class.java)?.copy(id = document.id)
                }
        } catch (e: Exception) {
            throw e
        }

    }

    suspend fun getUserData(userId: String): UserDto {
        return try {
            val user = firestore.collection("Users")
                .document(userId)
                .get()
                .await()
            if (!user.exists()) {
                addDocument(
                    documentId = userId,
                    collectionName = "Users",
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
}