package com.dmytro_turchyn.easypan.easypan.data.database

import com.dmytro_turchyn.easypan.easypan.data.dto.RecipeDto
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class FirestoreClient {
    private val firestore = Firebase.firestore


    suspend fun getCollection(collectionName: String) : List<RecipeDto> {
        return firestore.collection(collectionName)
            .get()
            .await()
            .documents
            .mapNotNull { document ->
                document.toObject(RecipeDto::class.java)
            }
    }
}