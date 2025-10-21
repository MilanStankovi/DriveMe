package com.example.driveme.ui.Screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.driveme.Data.Models.RideRequest
import com.example.driveme.Data.Models.User
import com.example.driveme.DriveMeTopBar
import com.example.driveme.ui.ViewModel.RideRequestViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun EditRideScreen(
    user: User,
    viewModel: RideRequestViewModel,
    onBack: () -> Unit
) {
    val ride by viewModel.ride.collectAsState()
    val context = LocalContext.current
    var comment by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var startLocation by remember { mutableStateOf<LatLng?>(null) }
    var endLocation by remember { mutableStateOf<LatLng?>(null) }

    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    // ucitava objekat ride
    LaunchedEffect(user.uid) {
        viewModel.getRideByUser(user.uid)
    }

    // popunjava polja za ride
    LaunchedEffect(ride) {
        ride?.let {
            comment = it.comment ?: ""
            selectedImageUri = it.imageUrl?.let { uriStr -> Uri.parse(uriStr) }
            startLocation = LatLng(it.pickupLat, it.pickupLng)
            if (it.destinationLat != null && it.destinationLng != null)
                endLocation = LatLng(it.destinationLat, it.destinationLng)
        }
    }

    Scaffold(
        topBar = { DriveMeTopBar(title = "Edit Ride") }
    ) { padding ->
        ride?.let { existingRide ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Edit Ride Request", style = MaterialTheme.typography.titleMedium)

                if (locationPermission.status.isGranted && startLocation != null) {
                    EditRideMap(
                        startLocation = startLocation!!,
                        endLocation = endLocation,
                        onEndLocationSelected = { endLocation = it }
                    )
                }

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comment") },
                    modifier = Modifier.fillMaxWidth()
                )

                ImagePicker { uri -> selectedImageUri = uri }

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val updated = existingRide.copy(
                            destinationLat = endLocation?.latitude,
                            destinationLng = endLocation?.longitude,
                            comment = comment,
                            imageUrl = selectedImageUri?.toString()
                        )
                        viewModel.updateRideRequest(user.uid, updated)
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Save Changes")
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
fun EditRideMap(
    startLocation: LatLng,
    endLocation: LatLng?,
    onEndLocationSelected: (LatLng) -> Unit
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(startLocation, 15f)
    }

    GoogleMap(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        cameraPositionState = cameraPositionState,
        onMapClick = { onEndLocationSelected(it) }
    ) {
        Marker(state = MarkerState(position = startLocation), title = "Start")
        endLocation?.let { Marker(state = MarkerState(position = it), title = "Destination") }
    }
}
