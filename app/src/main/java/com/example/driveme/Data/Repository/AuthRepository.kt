package com.example.driveme.Data.Repository


import AuthDataSource
import android.graphics.Bitmap
import android.net.Uri
import com.example.driveme.Data.Models.User

class AuthRepository(private val dataSource: AuthDataSource) {

    fun login(email: String, password: String, onResult: (Boolean, String?, User?) -> Unit) {
        dataSource.login(email, password, onResult)
    }

    fun register(
        email: String,
        password: String,
        user: User,
        imageUri: Uri?,
        imageBitmap: Bitmap?,
        onResult: (Boolean, String?) -> Unit
    ) {
        dataSource.register(email, password, user, imageUri, imageBitmap, onResult)
    }


}
