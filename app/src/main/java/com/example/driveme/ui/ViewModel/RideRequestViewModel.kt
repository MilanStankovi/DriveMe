package com.example.driveme.ui.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModel
import com.example.driveme.Data.Models.RideRequest
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

    fun loadRideRequests() {
        viewModelScope.launch {
            try {
                val result = repository.getAllRideRequests()
                _rides.value = result
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

    fun addRideRequest(ride: RideRequest) {
        viewModelScope.launch {
            try {
                repository.addRideRequest(ride)
                loadRideRequests()
            } catch (e: Exception) {

            }
        }
    }
}