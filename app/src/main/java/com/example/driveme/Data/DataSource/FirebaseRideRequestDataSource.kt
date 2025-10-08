package com.example.driveme.Data.DataSource
import android.net.Uri
import com.example.driveme.Data.Models.RideRequest
import com.example.driveme.Data.Models.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
class FirebaseRideRequestDataSource {
    private val db = Firebase.firestore
    private val collection = db.collection("ride_requests")

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

    suspend fun updateRideRequest(userId: String, updatedRide: RideRequest) {
        val snapshot = collection
            .whereEqualTo("userId", userId)  // pretraži ride po userId
            .get()
            .await()

        snapshot.documents.forEach { doc ->
            collection.document(doc.id).set(updatedRide).await()
        }
    }

    suspend fun deleteRideRequestByUserId(userId: String) {
        val snapshot = collection
            .whereEqualTo("userId", userId)
            .get()
            .await()

        val doc = snapshot.documents.firstOrNull()
        doc?.let {
            collection.document(it.id).delete().await()
        }
    }


    suspend fun getRideRequestByUserId(userId: String): RideRequest? {
        val snapshot = collection
            .whereEqualTo("userId", userId)
            .get()
            .await()
        return snapshot.documents.firstOrNull()?.toObject(RideRequest::class.java)
    }

    suspend fun addRideRequestWithImage(ride: RideRequest, imageUri: Uri?): RideRequest {
        val imageUrl = imageUri?.let { uri ->
            val storageRef: StorageReference = FirebaseStorage.getInstance()
                .reference.child("ride_images/${ride.userId}/${System.currentTimeMillis()}.jpg")
            storageRef.putFile(uri).await()
            storageRef.downloadUrl.await().toString()
        }

        val rideWithImage = ride.copy(imageUrl = imageUrl)
        collection.add(rideWithImage).await()
        return rideWithImage
    }


    suspend fun acceptRide(ride: RideRequest, user: User) {
        // Pretpostavljamo da RideRequest već ima ID u polju userId (ili možeš dodati posebno polje "id" za Firestore dokument)
        // Ako nema, treba da se prvo doda prilikom kreiranja vožnje
        val updatedRide = ride.copy(
            status = "running",
            takenBy = user.fullName
        )

        // Pronađi dokument u bazi koji odgovara ovom ride-u i setuj novi objekat
        val snapshot = collection
            .whereEqualTo("userId", ride.userId) // ili koristi polje "id" ako ga imaš
            .get()
            .await()

        val doc = snapshot.documents.firstOrNull()
        doc?.let {
            collection.document(it.id).set(updatedRide).await()
        }
    }


}