package com.cook.easypan.easypan.data.database

import com.cook.easypan.easypan.data.dto.RecipeDto
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class FirestoreClient {
    private val firestore = Firebase.firestore


    /**
     * Retrieves all documents from the specified Firestore collection and returns them as a list of `RecipeDto` objects.
     *
     * Each document is converted to a `RecipeDto`, with the Firestore document ID assigned to the `id` property.
     *
     * @param collectionName The name of the Firestore collection to fetch.
     * @return A list of `RecipeDto` objects representing the documents in the collection.
     */
    suspend fun getCollection(collectionName: String): List<RecipeDto> {
        return firestore.collection(collectionName)
            .get()
            .await()
            .documents
            .mapNotNull { document ->
                document.toObject(RecipeDto::class.java)?.copy(id = document.id)
            }
    }
}