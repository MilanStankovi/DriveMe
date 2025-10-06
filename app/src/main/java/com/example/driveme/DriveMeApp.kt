package com.example.driveme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.driveme.Data.Models.RideRequest
import com.example.driveme.ui.Navigation.DriveMeNavHost
import com.example.driveme.ui.Screens.HomeScreen
import com.example.driveme.ui.ViewModel.RideRequestViewModel
import com.example.driveme.ui.ViewModel.UserViewModel

@Composable
fun DriveMeApp(modifier: PaddingValues, viewModel: RideRequestViewModel,navController: NavHostController = rememberNavController(),
               userViewModel: UserViewModel){
    DriveMeNavHost(navController,viewModel,userViewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriveMeTopBar(
    title : String,
    modifier: Modifier = Modifier,
){
    CenterAlignedTopAppBar(
        title = {
            Text( text = title,
            color = Color.White)
        },
        modifier = modifier,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color(0xFF64B5F6)
        )
    )
}

data class BottomNavItem(val route: String, val icon: ImageVector, val label: String)

@Composable
fun DriveMeNavigationBar(modifier: Modifier = Modifier,
                                 onRideViewNavClicked: () -> Unit = {},
                                 onHomeNavClicked: () -> Unit = {}
                                 ){
    val items = listOf(
        BottomNavItem("Home", Icons.Default.Home, "Home"),
        BottomNavItem("RideView", Icons.Default.LocationOn, "Ride"),
    )
    //val currentBackStackEntry by navController.currentBackStackEntryAsState()
    //val currentRoute = currentBackStackEntry?.destination?.route

    NavigationBar() {
        items.forEach { item ->
            NavigationBarItem(
                selected =  false,
                onClick = {
                    if(item.route == "RideView"){
                        onRideViewNavClicked()
                    }
                    if(item.route == "Home"){
                        onHomeNavClicked()
                    }

                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}
