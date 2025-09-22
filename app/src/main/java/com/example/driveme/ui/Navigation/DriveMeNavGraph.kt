package com.example.driveme.ui.Navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.driveme.ui.Screens.HomeScreen
import com.example.driveme.ui.Screens.RideScreen
import com.example.driveme.ui.Screens.RideViewScreen

enum class DriveMeScreen(){
    Home,
    Ride,
    RideView,
    Profile
}

@Composable
fun DriveMeNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
){
    NavHost(
        navController = navController,
        startDestination = DriveMeScreen.Home.name,
    ){
        composable(route = DriveMeScreen.Home.name){
            HomeScreen(modifier,
                onRideViewNavClicked = {
                    navController.navigate(DriveMeScreen.RideView.name)
                },
                onButtonAddOrModRideClicked = {
                    navController.navigate(DriveMeScreen.Ride.name)
                })
        }
        composable(route = DriveMeScreen.Ride.name){
            RideScreen(modifier,
                onBack = {
                    navController.navigate(DriveMeScreen.Home.name)
                },
                onSubmit = {
                    navController.navigate(DriveMeScreen.Home.name)
                })
        }
        composable(route = DriveMeScreen.RideView.name){
            RideViewScreen(modifier,
                onHomeNavClicked = {
                    navController.navigate(DriveMeScreen.Home.name)
                })
        }

        composable(route = DriveMeScreen.Profile.name){

        }
    }
}