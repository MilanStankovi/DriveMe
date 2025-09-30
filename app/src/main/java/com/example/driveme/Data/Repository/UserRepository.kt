package com.example.driveme.Data.Repository

import com.example.driveme.Data.DataSource.FirebaseRideRequestDataSource
import com.example.driveme.Data.DataSource.FirebaseUserDataSource
import com.example.driveme.Data.Models.RideRequest
import com.example.driveme.Data.Models.User

class UserRepository(
    private val dataSource: FirebaseUserDataSource = FirebaseUserDataSource()
) {

    suspend fun getAllUsers(): List<User> {
        return dataSource.getAllUsers()
    }
}