package com.example.driveme.Data.DataSource

import com.example.driveme.Data.Models.RideRequest
import com.example.driveme.Data.Models.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class FirebaseUserDataSource {
    private val db = Firebase.firestore
    private val collection = db.collection("users")

    suspend fun getAllUsers(): List<User> {
        val snapshot = collection.get().await()
        return snapshot.mapNotNull { it.toObject(User::class.java) }
    }

}