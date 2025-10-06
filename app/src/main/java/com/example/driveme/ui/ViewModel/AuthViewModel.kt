package com.example.driveme.ui.ViewModel

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.driveme.Data.Models.User
import com.example.driveme.Data.Repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading
        repository.login(email, password) { success, error, user ->
            if (success && user != null) {
                _currentUser.value = user
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error(error ?: "Unknown error")
            }
        }
    }

    fun register(email: String, password: String, user: User, imageUri: Uri?, imageBitmap: Bitmap?) {
        _authState.value = AuthState.Loading
        repository.register(email, password, user, imageUri, imageBitmap) { success, error ->
            _authState.value = if (success) AuthState.Success else AuthState.Error(error ?: "Unknown error")
        }
    }

}
