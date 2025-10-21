package com.example.driveme.ui.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.driveme.Data.Models.RideRequest
import com.example.driveme.Data.Models.User
import com.example.driveme.DriveMeTopBar
import com.example.driveme.ui.ViewModel.RideRequestViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun DeleteRideScreen(
    user: User,
    viewModel: RideRequestViewModel,
    onBack: () -> Unit
) {
    val ride by viewModel.ride.collectAsState()

    // ucitavanje ride
    LaunchedEffect(user.uid) {
        viewModel.getRideByUser(user.uid)
    }

    Scaffold(
        topBar = { DriveMeTopBar(title = "Delete Ride") }
    ) { padding ->
        ride?.let { currentRide ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Delete Ride Request", style = MaterialTheme.typography.titleMedium)

                val startLocation = LatLng(currentRide.pickupLat, currentRide.pickupLng)
                val endLocation = if (currentRide.destinationLat != null && currentRide.destinationLng != null)
                    LatLng(currentRide.destinationLat, currentRide.destinationLng)
                else null

                DeleteRideMap(startLocation, endLocation)

                OutlinedTextField(
                    value = currentRide.comment ?: "",
                    onValueChange = {},
                    enabled = false,
                    label = { Text("Comment") },
                    modifier = Modifier.fillMaxWidth()
                )

                currentRide.imageUrl?.let {
                    Image(
                        painter = rememberAsyncImagePainter(it),
                        contentDescription = "Ride Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModel.deleteRideRequest(user.uid) // briše ride po userId
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete Ride")
                }

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Cancel")
                }
            }
        } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun DeleteRideMap(startLocation: LatLng, endLocation: LatLng?) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(startLocation, 15f)
    }

    GoogleMap(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        cameraPositionState = cameraPositionState,
        onMapClick = { }
    ) {
        Marker(state = MarkerState(position = startLocation), title = "Start")
        endLocation?.let { Marker(state = MarkerState(position = it), title = "Destination") }
    }
}
