package com.example.driveme.ui.Screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.driveme.Data.Models.User
import com.example.driveme.DriveMeNavigationBar
import com.example.driveme.DriveMeTopBar
import com.example.driveme.ui.ViewModel.UserViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onRideViewNavClicked: () -> Unit = {},
    onButtonAddOrModRideClicked: () -> Unit = {},
    onProfileNavClicked: () -> Unit = {},
    user: User,
    userViewModel: UserViewModel = UserViewModel()
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp

    // Pretvaramo StateFlow u Compose state
    val users by userViewModel.users.collectAsState()

    // Učitavamo korisnike kada se ekran prvi put pojavi
    LaunchedEffect(Unit) {
        userViewModel.loadUsers()
    }

    Scaffold(
        topBar = { DriveMeTopBar(title = "Home") },
        bottomBar = {
            DriveMeNavigationBar(
                onRideViewNavClicked = onRideViewNavClicked
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "Hello ${user.username} 👋",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            Spacer(Modifier.height(24.dp))

            if (screenWidth < 600) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    RideButtons(onButtonAddOrModRideClicked)
                    PointsSection(user)
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RideButtons(onButtonAddOrModRideClicked)
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        PointsSection(user)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "Leaderboard",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.Gray,
                modifier = Modifier.padding(start = 8.dp)
            )

            if (users.isEmpty()) {
                // Loading indikator ako nema korisnika
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .weight(1f)
                        .border(3.dp, Color(0xFF87CEFA), RoundedCornerShape(24.dp))
                        .padding(8.dp)
                ) {
                    val sortedUsers = users.sortedByDescending { it.points }
                    itemsIndexed(sortedUsers) { index, userItem ->
                        LeaderboardUserItem(rank = index + 1, user = userItem)
                    }
                }
            }
        }
    }
}

@Composable
fun RideButtons(onButtonAddOrModRideClicked: () -> Unit) {
    Button(
        onClick = { onButtonAddOrModRideClicked() },
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6495ED)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
        Spacer(Modifier.width(8.dp))
        Text("Add Ride")
    }

    Button(
        onClick = { onButtonAddOrModRideClicked() },
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6495ED)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
        Spacer(Modifier.width(8.dp))
        Text("Edit Ride")
    }

    Button(
        onClick = {},
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6495ED)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
        Spacer(Modifier.width(8.dp))
        Text("Delete Ride")
    }
}

@Composable
fun PointsSection(user: User) {
    Text(
        text = "Points:",
        style = MaterialTheme.typography.titleLarge,
        color = Color.Green
    )
    Text(
        text = "${user.points}",
        style = MaterialTheme.typography.displaySmall,
        color = Color.Green
    )
}

@Composable
fun LeaderboardUserItem(rank: Int, user: User) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$rank.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.width(32.dp)
            )
            Text(
                text = user.username,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = user.points.toString(),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
