package com.example.driveme.Data.DataSource
import com.example.driveme.Data.Models.RideRequest
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
class FirebaseRideRequestDataSource {
    private val db = Firebase.firestore
    private val collection = db.collection("ride_request")

    suspend fun addRideRequest(ride: RideRequest) {
        collection.add(ride).await()
    }

    suspend fun getAllRideRequests(): List<RideRequest> {
        val snapshot = collection.get().await()
        return snapshot.mapNotNull { it.toObject(RideRequest::class.java) }
    }

    suspend fun getRideRequestById(id: String): RideRequest? {
        val doc = collection.document(id).get().await()
        return if (doc.exists()) doc.toObject(RideRequest::class.java) else null
    }
}