package com.example.driveme.Data.Repository

import com.example.driveme.Data.DataSource.FirebaseRideRequestDataSource
import com.example.driveme.Data.Models.RideRequest

class RideRequestRepository(
    private val dataSource: FirebaseRideRequestDataSource = FirebaseRideRequestDataSource()
) {
    suspend fun addRideRequest(ride: RideRequest) {
        dataSource.addRideRequest(ride)
    }

    suspend fun getAllRideRequests(): List<RideRequest> {
        return dataSource.getAllRideRequests()
    }

    suspend fun getRideRequestById(id: String): RideRequest? {
        return dataSource.getRideRequestById(id)
    }
}