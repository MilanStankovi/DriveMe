package com.example.driveme.ui.Navigation

import com.example.driveme.ui.Screens.AuthScreens.LoginScreen
import com.example.driveme.ui.Screens.AuthScreens.RegisterScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.driveme.Data.DataSource.AuthDataSource
import com.example.driveme.Data.Repository.AuthRepository
import com.example.driveme.ui.Screens.*
import com.example.driveme.ui.ViewModel.AuthViewModel

enum class DriveMeScreen {
    Home,
    Ride,
    RideView,
    Profile,
    Login,
    Register
}

@Composable
fun DriveMeNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val authViewModel = remember {
        AuthViewModel(
            AuthRepository(
                AuthDataSource(context) // ✅ sad prosleđujemo context
            )
        )
    }

    NavHost(
        navController = navController,
        startDestination = DriveMeScreen.Login.name
    ) {
        composable(DriveMeScreen.Login.name) {
            LoginScreen(
                viewModel = authViewModel,
                onAuthSuccess = {
                    val user = authViewModel.currentUser.value
                    navController.navigate(DriveMeScreen.Home.name) {
                        popUpTo(DriveMeScreen.Login.name) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(DriveMeScreen.Register.name)
                }
            )
        }

        composable(DriveMeScreen.Register.name) {
            RegisterScreen(
                viewModel = authViewModel,
                onAuthSuccess = {
                    navController.navigate(DriveMeScreen.Home.name) {
                        popUpTo(DriveMeScreen.Register.name) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(DriveMeScreen.Login.name)
                }
            )
        }

        composable(DriveMeScreen.Home.name) {
            val currentUser by authViewModel.currentUser.collectAsState()
            if(currentUser != null) {
                HomeScreen(
                    modifier,
                    onRideViewNavClicked = { navController.navigate(DriveMeScreen.RideView.name) },
                    onButtonAddOrModRideClicked = { navController.navigate(DriveMeScreen.Ride.name) },
                    user = currentUser!!
                )
            }
        }

        composable(DriveMeScreen.Ride.name) {
            RideScreen(
                modifier,
                onBack = { navController.navigate(DriveMeScreen.Home.name) },
                onSubmit = { navController.navigate(DriveMeScreen.Home.name) }
            )
        }

        composable(DriveMeScreen.RideView.name) {
            RideViewScreen(
                modifier,
                onHomeNavClicked = { navController.navigate(DriveMeScreen.Home.name) }
            )
        }

        composable(DriveMeScreen.Profile.name) {
            // TODO: Profile screen
        }
    }
}
