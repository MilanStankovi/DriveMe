package com.example.driveme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.driveme.ui.theme.DriveMeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DriveMeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DriveMeApp(innerPadding)
                }
            }
        }
    }
}