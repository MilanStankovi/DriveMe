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

    suspend fun updateUser(user: User) {
        dataSource.updateUser(user)
    }

    suspend fun updateUserLocation(uid: String, lat: Double, lng: Double) {
        dataSource.updateUserLocation(uid, lat, lng)
    }

    suspend fun getUserById(uid: String): User? {
        return dataSource.getUserById(uid)
    }

    fun listenToUsers(onUsersChanged: (List<User>) -> Unit) {
        dataSource.listenToUsers(onUsersChanged)
    }

}