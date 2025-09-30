package com.example.driveme.ui.Screens

import android.Manifest
import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
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
    onBack: () -> Unit = {},
    user: User
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    var startLocation by remember { mutableStateOf<LatLng?>(null) }
    var endLocation by remember { mutableStateOf<LatLng?>(null) }
    var comment by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        }
    }

    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (locationPermissionState.status.isGranted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let { startLocation = LatLng(it.latitude, it.longitude) }
            }
        }
    }

    Scaffold(
        topBar = { DriveMeTopBar(title = "Home") },
        bottomBar = {
            Column {
                RideButtons(onSubmit = onSubmit, onBack = onBack)
                DriveMeNavigationBar(onRideViewNavClicked = onBack)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
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


            ImagePicker { uri -> selectedImageUri = uri }
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
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            modifier = Modifier.weight(1f),
            onClick = onSubmit,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
        ) {
            Text("Submit")
        }
        Spacer(modifier = Modifier.width(16.dp))
        Button(
            modifier = Modifier.weight(1f),
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
        ) {
            Text("Back")
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ImagePicker(onImageSelected: (Uri) -> Unit) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current


    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    //Za galeriju
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            onImageSelected(it)
        }
    }

    // Za kameru
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraImageUri?.let {
                selectedImageUri = it
                onImageSelected(it)
            }
        }
    }


    fun createImageUri(): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "ride_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Button(modifier = Modifier.weight(1f), onClick = {
                galleryLauncher.launch("image/*")
            }) {
                Text("Izaberi iz galerije")
            }


            Button(modifier = Modifier.weight(1f), onClick = {
                if (cameraPermissionState.status.isGranted) {
                    cameraImageUri = createImageUri()
                    cameraImageUri?.let { cameraLauncher.launch(it) }
                } else {
                    cameraPermissionState.launchPermissionRequest()
                }
            }) {
                Text("Slikaj kamerom")
            }
        }


        selectedImageUri?.let { uri ->
            Spacer(modifier = Modifier.height(8.dp))
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = "Izabrana slika",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
        }
    }
}


