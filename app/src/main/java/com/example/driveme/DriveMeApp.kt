package com.example.driveme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.example.driveme.Data.Models.RideRequest
import com.example.driveme.ui.ViewModel.RideRequestViewModel

@Composable
fun DriveMeApp(modifier: PaddingValues, viewModel: RideRequestViewModel){
    LaunchedEffect(Unit) {
        val ride = RideRequest(
            userId = "user123",
            pickupLat = 43.321,
            pickupLng = 21.895,
            destinationLat = 43.333,
            destinationLng = 21.901,
            status = "open"
        )
        viewModel.addRideRequest(ride)
    }
}
