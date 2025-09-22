package com.example.driveme.ui.Screens

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.driveme.DriveMeNavigationBar
import com.example.driveme.DriveMeTopBar
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RideScreen(
    modifier: Modifier = Modifier,
    onSubmit: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    var startLocation by remember { mutableStateOf<LatLng?>(null) }
    var endLocation by remember { mutableStateOf<LatLng?>(null) }
    var comment by remember { mutableStateOf("") }

    //Ovde trazim permisiju
    LaunchedEffect(Unit) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        }
    }

    //Postavljanje trenutne lokacije
    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (locationPermissionState.status.isGranted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    startLocation = LatLng(it.latitude, it.longitude)
                }
            }
        }
    }

    Scaffold(
        topBar = { DriveMeTopBar(title = "Home") },
        bottomBar = { DriveMeNavigationBar(onRideViewNavClicked = onBack) }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (locationPermissionState.status.isGranted && startLocation != null) {
                TaskMap(
                    startLocation = startLocation!!,
                    endLocation = endLocation,
                    onEndLocationSelected = { endLocation = it }
                )

                LocationsRide(startLocation = startLocation!!, endLocation = endLocation)
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Aplikacija zahteva dozvolu za pristup lokaciji.")
                    Button(onClick = { locationPermissionState.launchPermissionRequest() }) {
                        Text("Dozvoli pristup lokaciji")
                    }
                }
            }

            CommentTextField(comment = comment) { comment = it }
            RideButtons(onSubmit = onSubmit, onBack = onBack)
        }
    }
}

@Composable
fun TaskMap(
    startLocation: LatLng,
    endLocation: LatLng?,
    onEndLocationSelected: (LatLng) -> Unit,
    modifier: Modifier = Modifier
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(startLocation, 15f)
    }

    GoogleMap(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp),
        cameraPositionState = cameraPositionState,
        onMapClick = { latLng -> onEndLocationSelected(latLng) }
    ) {

        Marker(state = MarkerState(position = startLocation), title = "Start")

        endLocation?.let { Marker(state = MarkerState(position = it), title = "Destination") }
    }
}

@Composable
fun LocationsRide(
    startLocation: LatLng,
    endLocation: LatLng?
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Start: ${startLocation.latitude}, ${startLocation.longitude}")
        Text("Destination: ${endLocation?.latitude ?: "?"}, ${endLocation?.longitude ?: "?"}")
    }
}

@Composable
fun CommentTextField(comment: String, onCommentChange: (String) -> Unit) {
    OutlinedTextField(
        value = comment,
        onValueChange = onCommentChange,
        label = { Text("Unesite komentar") },
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        singleLine = false,
        maxLines = 5
    )
}

@Composable
fun RideButtons(
    onSubmit: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(onClick = onSubmit, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))) {
            Text("Submit")
        }
        Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))) {
            Text("Back")
        }
    }
}
