package com.example.driveme.ui.Screens

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.driveme.ui.ViewModel.RideRequestViewModel



@Composable
fun RideViewScreen(
    modifier: Modifier = Modifier,
    onHomeNavClicked: () -> Unit = {},
    onProfileNavClicked: () -> Unit = {},
    rideRequestViewModel: RideRequestViewModel = viewModel()
) {

}
