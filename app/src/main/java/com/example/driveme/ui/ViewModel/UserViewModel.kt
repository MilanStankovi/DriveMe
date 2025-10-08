package com.example.driveme.ui.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.driveme.Data.Models.RideRequest
import com.example.driveme.Data.Models.User
import com.example.driveme.Data.Repository.RideRequestRepository
import com.example.driveme.Data.Repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    fun loadUsers() {
        viewModelScope.launch {
            try {
                val result = repository.getAllUsers()
                _users.value = result
            } catch (e: Exception) {

            }
        }
    }

    fun createRidePoints(user: User) {
        viewModelScope.launch {
            try {
                user.points += 70
                repository.updateUser(user)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateUserLocation(uid: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            try {
                repository.updateUserLocation(uid, lat, lng)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getUserById(uid: String, onResult: (User?) -> Unit) {
        viewModelScope.launch {
            try {
                val user = repository.getUserById(uid)
                onResult(user)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(null)
            }
        }
    }
}