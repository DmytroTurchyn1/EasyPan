package com.cook.easypan.easypan.data.database

import com.cook.easypan.easypan.data.dto.RecipeDto
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class FirestoreClient {
    private val firestore = Firebase.firestore

    suspend fun getCollection(collectionName: String): List<RecipeDto> {
        return try {
            firestore.collection(collectionName)
                .get()
                .await()
                .documents
                .mapNotNull { document ->
                    document.toObject(RecipeDto::class.java)?.copy(id = document.id)
                }
        } catch (e: Exception) {
            throw e
        }

    }
}