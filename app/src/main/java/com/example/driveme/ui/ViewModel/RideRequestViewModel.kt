package com.example.driveme.ui.ViewModel
import android.net.Uri
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModel
import com.example.driveme.Data.Models.RideRequest
import com.example.driveme.Data.Models.User
import com.example.driveme.Data.Repository.RideRequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
class RideRequestViewModel(
    private val repository: RideRequestRepository = RideRequestRepository()
): ViewModel(){
    private val _rides = MutableStateFlow<List<RideRequest>>(emptyList())
    val rides: StateFlow<List<RideRequest>> = _rides

    private val _ride = MutableStateFlow<RideRequest?>(null)
    val ride: StateFlow<RideRequest?> = _ride

    init {
        viewModelScope.launch {
            repository.observeAllRideRequests().collect { list ->
                _rides.value = list
            }
        }
    }

    fun loadRideRequests() {
        viewModelScope.launch {
            try {
                repository.observeAllRideRequests().collect { list ->
                    _rides.value = list
                }
            } catch (e: Exception) {

            }
        }
    }

    fun loadRideRequestById(id : String) {
        viewModelScope.launch {
            try {
                val result = repository.getRideRequestById(id)
                _ride.value = result
            } catch (e: Exception) {

            }
        }
    }

    fun addRideRequest(ride: RideRequest, imageUri: Uri?) {
        viewModelScope.launch {
            try {
                repository.addRideRequest(ride, imageUri)
                loadRideRequests()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun updateRideRequest(userId: String, updatedRide: RideRequest) {
        viewModelScope.launch {
            try {
                repository.updateRideRequest(userId, updatedRide)
                loadRideRequests()
            } catch (e: Exception) { }
        }
    }

    fun deleteRideRequest(userId: String) {
        viewModelScope.launch {
            try {
                repository.deleteRideRequest(userId)
                loadRideRequests()
            } catch (e: Exception) { }
        }
    }

    fun getRideByUser(userId: String) {
        viewModelScope.launch {
            try {
                val ride = repository.getRideRequestByUserId(userId)
                _ride.value = ride
            } catch (e: Exception) {
            }
        }
    }


    fun acceptRide(ride: RideRequest, user: User) {
        viewModelScope.launch {
            try {
                repository.acceptRide(ride, user)
                loadRideRequests()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


}