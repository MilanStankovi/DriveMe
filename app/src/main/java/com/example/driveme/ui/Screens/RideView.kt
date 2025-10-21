package com.example.driveme.ui.Screens

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.example.driveme.Data.Models.User
import com.example.driveme.ui.ViewModel.RideRequestViewModel
import com.example.driveme.ui.ViewModel.UserViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

fun distanceInMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
    val results = FloatArray(1)
    Location.distanceBetween(lat1, lon1, lat2, lon2, results)
    return results[0]
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission")
@Composable
fun RideViewScreen(
    currentUser: User,
    modifier: Modifier = Modifier,
    rideViewModel: RideRequestViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
    onHomeNavClicked: () -> Unit
) {
    //Za notifikaciju
    val notificationPermission = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)

    LaunchedEffect(Unit) {
        if (!notificationPermission.status.isGranted) {
            notificationPermission.launchPermissionRequest()
        }
    }


    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 🔹 Ucitavanje podataka i pracenje korisnika u realnom vremenu
    LaunchedEffect(Unit) {
        rideViewModel.loadRideRequests()
        userViewModel.startUserListener()
    }

    val rideRequests by rideViewModel.rides.collectAsState()
    val users by userViewModel.users.collectAsState()

    // 🔹 Lokacija trenutnog korisnika
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

    val notifiedRides = remember { mutableStateListOf<String>() }

    // 🔹 Periodicno azuriranje lokacije
    LaunchedEffect(currentUser.uid, locationPermission.status.isGranted) {
        if (locationPermission.status.isGranted) {
            while (true) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        currentLocation = LatLng(location.latitude, location.longitude)
                        scope.launch {
                            userViewModel.updateUserLocation(
                                currentUser.uid,
                                location.latitude,
                                location.longitude
                            )
                            rideViewModel.rides.value.forEach { ride ->
                                val distance = distanceInMeters(
                                    location.latitude,
                                    location.longitude,
                                    ride.pickupLat,
                                    ride.pickupLng
                                )
                                if (ride.status == "open" && distance <= 50 && distance <= 50 && !notifiedRides.contains(ride.userId)) {
                                    showNearbyNotification(
                                        context.applicationContext,
                                        "Objekat je u blizini!",
                                        "Nalaziš se blizu ride lokacije – možeš da ga prihvatiš."
                                    )
                                    notifiedRides.add(ride.userId)
                                }
                            }
                        }
                    }
                }
                delay(10_000)
            }
        }
    }

    // 🔹 Filteri
    var statusFilter by remember { mutableStateOf("") }
    var radiusKm by remember { mutableStateOf("") }
    var selectedRide by remember { mutableStateOf<RideRequest?>(null) }

    // 🔹 Kamera
    val cameraPositionState = rememberCameraPositionState {
        currentLocation?.let {
            position = CameraPosition.fromLatLngZoom(it, 12f)
        }
    }

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


    val filteredRequests = rideRequests.filter { ride ->
        val matchesStatus = if (statusFilter.isBlank()) true else ride.status.equals(statusFilter, ignoreCase = true)
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

        matchesStatus && matchesRadius
    }

    // UI
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ride Requests & Users Map") },
                navigationIcon = {
                    IconButton(onClick = onHomeNavClicked) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = locationPermission.status.isGranted)
            ) {
                // Ride request markeri (crveni)
                filteredRequests.forEach { ride ->
                    val pos = LatLng(ride.pickupLat, ride.pickupLng)
                    Marker(
                        state = MarkerState(position = pos),
                        title = "Ride Request (${ride.status})",
                        snippet = "Tap for details",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
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

                // 🔹 Ostali korisnici (plavi markeri u realnom vremenu)
                users.filter { it.uid != currentUser.uid }.forEach { user ->
                    if (user.lat != null && user.lng != null) {
                        Marker(
                            state = MarkerState(position = LatLng(user.lat!!, user.lng!!)),
                            title = "User: ${user.fullName.ifBlank { user.username }}",
                            snippet = "Points: ${user.points}",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                        )
                    }
                }
            }

            // 🔹 Filter panel
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
                    label = { Text("Status (open, running...)") },
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

            // Info kartica za ride
            selectedRide?.let { ride ->
                val context = LocalContext.current
                var destinationAddress by remember { mutableStateOf("Ucitavam adresu...") }

                LaunchedEffect(ride.destinationLat, ride.destinationLng) {
                    if (ride.destinationLat != null && ride.destinationLng != null) {
                        destinationAddress = getAddressFromLatLng(context, ride.destinationLat!!, ride.destinationLng!!)
                    } else {
                        destinationAddress = "Nema destinacije"
                    }
                }
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
                        Text("Destinacija: $destinationAddress")
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

                        val canAccept = ride.status == "open" &&
                                currentLocation != null &&
                                distanceInMeters(
                                    currentLocation!!.latitude,
                                    currentLocation!!.longitude,
                                    ride.pickupLat,
                                    ride.pickupLng
                                ) <= 10

                        Button(
                            onClick = {
                                rideViewModel.acceptRide(ride, currentUser)
                                userViewModel.acceptRidePoints(currentUser)
                                selectedRide = null
                            },
                            enabled = canAccept,
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(top = 8.dp)
                        ) {
                            Text("Accept")
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

fun showNearbyNotification(context: Context, title: String, message: String) {
    val appContext = context.applicationContext
    val channelId = "ride_nearby_channel"
    val notificationId = (0..9999).random()

    val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Proveri da li kanal vec postoji
    if (notificationManager.getNotificationChannel(channelId) == null) {
        val channel = NotificationChannel(
            channelId,
            "Nearby Ride Notifications",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifikacije kada je objekat u blizini"
        }
        notificationManager.createNotificationChannel(channel)
    }

    val notification = NotificationCompat.Builder(appContext, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .build()

    with(NotificationManagerCompat.from(appContext)) {
        notify(notificationId, notification)
    }
}


