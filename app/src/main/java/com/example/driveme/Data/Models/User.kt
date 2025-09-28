package com.example.driveme.Data.Models

data class User(
    val uid: String = "",
    val username: String = "",
    val fullName: String = "",
    val phone: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val points: Int = 0
)
