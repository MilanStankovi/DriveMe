package com.example.driveme.Data.Repository

import android.net.Uri
import com.example.driveme.Data.DataSource.FirebaseRideRequestDataSource
import com.example.driveme.Data.Models.RideRequest
import com.example.driveme.Data.Models.User

class RideRequestRepository(
    private val dataSource: FirebaseRideRequestDataSource = FirebaseRideRequestDataSource()
) {
    suspend fun addRideRequest(ride: RideRequest, imageUri: Uri?) {
        dataSource.addRideRequestWithImage(ride, imageUri)
    }

    suspend fun getAllRideRequests(): List<RideRequest> {
        return dataSource.getAllRideRequests()
    }

    suspend fun getRideRequestById(id: String): RideRequest? {
        return dataSource.getRideRequestById(id)
    }

    suspend fun updateRideRequest(userId: String, updatedRide: RideRequest) {
        dataSource.updateRideRequest(userId, updatedRide)
    }

    suspend fun deleteRideRequest(userId: String) {
        dataSource.deleteRideRequestByUserId(userId)
    }

    suspend fun getRideRequestByUserId(userId: String): RideRequest? {
        return dataSource.getRideRequestByUserId(userId)
    }

    suspend fun acceptRide(ride: RideRequest, user: User) {
        dataSource.acceptRide(ride, user)
    }


}