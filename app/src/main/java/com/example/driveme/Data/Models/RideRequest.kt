package com.example.driveme.Data.Models

data class RideRequest(
    val id: String = "",
    val userId: String = "",
    val pickupLat: Double = 0.0,
    val pickupLng: Double = 0.0,
    val destinationLat: Double? = null,
    val destinationLng: Double? = null,
    val timeCreated: Long = System.currentTimeMillis(),
    val status: String = "open",
    val takenBy: List<String> = emptyList()
)
