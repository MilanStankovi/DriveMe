package com.example.driveme.Data.DataSource

import com.example.driveme.Data.Models.RideRequest
import com.example.driveme.Data.Models.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseUserDataSource {
    private val db = Firebase.firestore
    private val collection = db.collection("users")

    fun observeAllUsers() = callbackFlow<List<User>> {
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val users = snapshot?.documents?.mapNotNull { it.toObject(User::class.java) } ?: emptyList()
            trySend(users)
        }
        awaitClose { listener.remove() }
    }

    suspend fun updateUser(user: User) {
        db.collection("users").document(user.uid).update("points", user.points)

    }

    suspend fun updateUserLocation(uid: String, lat: Double, lng: Double) {
        collection.document(uid).update(mapOf("lat" to lat, "lng" to lng)).await()
    }


    suspend fun getUserById(uid: String): User? {
        val doc = db.collection("users").document(uid).get().await()
        return doc.toObject(User::class.java)
    }
}