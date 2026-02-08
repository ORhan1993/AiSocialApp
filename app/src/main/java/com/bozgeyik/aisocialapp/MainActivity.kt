package com.bozgeyik.aisocialapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bozgeyik.aisocialapp.presentation.FeedScreen
import com.bozgeyik.aisocialapp.presentation.LoginScreen
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val auth = FirebaseAuth.getInstance()

        val startDestination = if (auth.currentUser != null) "feed_screen" else "login_screen"

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {

                        composable("login_screen") {
                            LoginScreen(navController = navController)
                        }

                        composable("feed_screen") {
                            FeedScreen()
                        }
                    }
                }
            }
        }
    }
}
