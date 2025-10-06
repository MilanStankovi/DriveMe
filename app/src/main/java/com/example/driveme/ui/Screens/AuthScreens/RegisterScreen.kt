package com.example.driveme.ui.Screens.AuthScreens

import android.Manifest
import android.graphics.Bitmap
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.driveme.Data.Models.User
import com.example.driveme.ui.ViewModel.AuthState
import com.example.driveme.ui.ViewModel.AuthViewModel

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier,
    onAuthSuccess: (user: User) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    // Može biti samo jedna slika — iz galerije ili kamerom
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val authState by viewModel.authState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // 📁 Launcher za izbor slike iz galerije
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            selectedImageBitmap = null // reset ako je ranije slikano
        }
    }

    // 📸 Launcher za kameru
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            selectedImageBitmap = it
            selectedImageUri = null // reset ako je ranije izabrano iz galerije
        }
    }

    // 🪪 Launcher za runtime CAMERA permisiju
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (authState is AuthState.Loading) CircularProgressIndicator()
            if (authState is AuthState.Error) {
                Text(
                    (authState as AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                    Text("Select from Gallery")
                }
                Button(onClick = {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }) {
                    Text("Take Photo")
                }
            }

            // 🖼️ Prikaz slike ako postoji
            when {
                selectedImageUri != null -> {
                    Image(
                        painter = rememberAsyncImagePainter(selectedImageUri),
                        contentDescription = "Selected Image",
                        modifier = Modifier
                            .size(120.dp)
                            .padding(8.dp),
                        contentScale = ContentScale.Crop
                    )
                }
                selectedImageBitmap != null -> {
                    Image(
                        bitmap = selectedImageBitmap!!.asImageBitmap(),
                        contentDescription = "Captured Image",
                        modifier = Modifier
                            .size(120.dp)
                            .padding(8.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val user = User(
                        uid = "",
                        username = username,
                        fullName = fullName,
                        phone = phone,
                        email = email,
                        photoUrl = null
                    )

                    // Prosleđuje se i URI i Bitmap
                    viewModel.register(email, password, user, selectedImageUri, selectedImageBitmap)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Register")
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = onNavigateToLogin) {
                Text("Already have an account? Login")
            }
        }
    }

    // ✅ Kada je registracija uspešna
    LaunchedEffect(authState, currentUser) {
        if (authState is AuthState.Success && currentUser != null) {
            onAuthSuccess(currentUser!!)
        }
    }
}
