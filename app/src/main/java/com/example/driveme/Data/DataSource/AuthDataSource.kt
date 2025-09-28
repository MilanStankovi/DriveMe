package com.example.driveme.Data.DataSource

import android.content.Context
import android.net.Uri
import com.example.driveme.Data.Models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream

class AuthDataSource(private val context: Context) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun register(
        email: String,
        password: String,
        user: User,
        imageUri: Uri?,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid == null || uid.isBlank()) {
                    onResult(false, "UID not found")
                    return@addOnSuccessListener
                }

                if (imageUri != null) {
                    val ref = storage.reference.child("profile_images/$uid.jpg")
                    ref.putFile(imageUri) // ✅ direktno koristi picked URI
                        .addOnSuccessListener {
                            ref.downloadUrl
                                .addOnSuccessListener { uri ->
                                    val newUser = user.copy(uid = uid, photoUrl = uri.toString())
                                    saveUserToFirestore(newUser, onResult)
                                }
                                .addOnFailureListener { error ->
                                    onResult(false, "Failed to get image URL: ${error.message}")
                                }
                        }
                        .addOnFailureListener { error ->
                            onResult(false, "Image upload failed: ${error.message}")
                        }
                } else {
                    val newUser = user.copy(uid = uid)
                    saveUserToFirestore(newUser, onResult)
                }
            }
            .addOnFailureListener { error ->
                onResult(false, "Registration failed: ${error.message}")
            }
    }


    private fun saveUserToFirestore(user: User, onResult: (Boolean, String?) -> Unit) {
        firestore.collection("users").document(user.uid)
            .set(user)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { onResult(false, it.message) }
    }

    fun login(email: String, password: String, onResult: (Boolean, String?, User?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: ""
                firestore.collection("users").document(uid)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val user = snapshot.toObject(User::class.java)
                        if (user != null) {
                            onResult(true, null, user)
                        } else {
                            onResult(false, "User not found", null)
                        }
                    }
                    .addOnFailureListener {
                        onResult(false, it.message, null)
                    }
            }
            .addOnFailureListener {
                onResult(false, it.message, null)
            }
    }

    // Helper za konverziju Uri -> File
    private fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("profile", ".jpg", context.cacheDir)
            val outputStream = FileOutputStream(tempFile)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
