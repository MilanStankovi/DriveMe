package com.example.driveme.ui.Screens

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.driveme.Data.Models.RideRequest
import com.example.driveme.ui.ViewModel.RideRequestViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission")
@Composable
fun RideViewScreen(
    modifier: Modifier = Modifier,
    viewModel: RideRequestViewModel = viewModel(),
    onHomeNavClicked: () -> Unit
) {
    // 🔹 Učitavanje svih RideRequest objekata
    LaunchedEffect(Unit) {
        viewModel.loadRideRequests()
    }

    val rideRequests by viewModel.rides.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 🔹 Lokacija korisnika
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    // 🔹 Dohvati trenutnu lokaciju
    LaunchedEffect(locationPermission.status.isGranted) {
        if (locationPermission.status.isGranted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    currentLocation = LatLng(it.latitude, it.longitude)
                }
            }
        } else {
            locationPermission.launchPermissionRequest()
        }
    }

    // 🔹 Filteri
    var statusFilter by remember { mutableStateOf("") }
    var fromTime by remember { mutableStateOf("") }
    var toTime by remember { mutableStateOf("") }
    var radiusKm by remember { mutableStateOf("") }
    var selectedRide by remember { mutableStateOf<RideRequest?>(null) }

    // 🔹 Kamera
    val cameraPositionState = rememberCameraPositionState {
        currentLocation?.let {
            position = CameraPosition.fromLatLngZoom(it, 12f)
        }
    }

    // 🔹 Haversine formula za radius
    fun withinRadius(lat1: Double, lon1: Double, lat2: Double, lon2: Double, radiusKm: Double): Boolean {
        val R = 6371
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) + cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = R * c
        return distance <= radiusKm
    }

    // 🔹 Filtriranje
    val filteredRequests = rideRequests.filter { ride ->
        val matchesStatus = if (statusFilter.isBlank()) true else ride.status.equals(statusFilter, ignoreCase = true)
        val matchesTime = try {
            val from = if (fromTime.isBlank()) 0L else fromTime.toLong()
            val to = if (toTime.isBlank()) Long.MAX_VALUE else toTime.toLong()
            ride.timeCreated in from..to
        } catch (_: Exception) { true }

        val matchesRadius = try {
            if (radiusKm.isNotBlank() && currentLocation != null) {
                withinRadius(
                    currentLocation!!.latitude,
                    currentLocation!!.longitude,
                    ride.pickupLat,
                    ride.pickupLng,
                    radiusKm.toDouble()
                )
            } else true
        } catch (_: Exception) { true }

        matchesStatus && matchesTime && matchesRadius
    }

    // 🔹 Scaffold
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ride Requests Map") },
                navigationIcon = {
                    IconButton(onClick = onHomeNavClicked) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
        ) {

            // 🔹 Google Mapa
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = locationPermission.status.isGranted)
            ) {
                filteredRequests.forEach { ride ->
                    val pos = LatLng(ride.pickupLat, ride.pickupLng)
                    Marker(
                        state = MarkerState(position = pos),
                        title = "Ride Request (${ride.status})",
                        snippet = "Tap for details",
                        onClick = {
                            selectedRide = ride
                            scope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(pos, 14f)
                                )
                            }
                            false
                        }
                    )
                }
            }

            // 🔹 Filter panel (transparent preko mape)
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(8.dp)
                    .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedTextField(
                    value = statusFilter,
                    onValueChange = { statusFilter = it },
                    label = { Text("Status (open, taken...)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = fromTime,
                    onValueChange = { fromTime = it },
                    label = { Text("From (timestamp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = toTime,
                    onValueChange = { toTime = it },
                    label = { Text("To (timestamp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = radiusKm,
                    onValueChange = { radiusKm = it },
                    label = { Text("Radius (km)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 🔹 Info kartica za marker
            selectedRide?.let { ride ->
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Status: ${ride.status}", style = MaterialTheme.typography.titleMedium)
                        Text("Created: ${ride.timeCreated}")
                        Text("Destination: ${ride.destinationLat}, ${ride.destinationLng}")
                        ride.comment?.let { Text("Comment: $it") }
                        ride.imageUrl?.let {
                            Image(
                                painter = rememberAsyncImagePainter(it),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .padding(top = 8.dp)
                            )
                        }
                        Button(
                            onClick = { selectedRide = null },
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(top = 8.dp)
                        ) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }
}
