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
import com.bozgeyik.aisocialapp.ui.screens.AddStoryScreen
import com.bozgeyik.aisocialapp.ui.screens.ChatScreen
import com.bozgeyik.aisocialapp.ui.screens.LoginScreen
import com.bozgeyik.aisocialapp.ui.screens.MainLayout
import com.bozgeyik.aisocialapp.ui.screens.SearchScreen
import com.bozgeyik.aisocialapp.ui.screens.SignupScreen
import com.bozgeyik.aisocialapp.ui.screens.SplashScreen
import com.bozgeyik.aisocialapp.ui.theme.AiSocialAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AiSocialAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // Navigasyon Yapısı
                    NavHost(navController = navController, startDestination = "splash") {

                        // 1. AÇILIŞ EKRANI (Oto-Login Kontrolü Burada)
                        composable("splash") {
                            SplashScreen(
                                onNavigateToHome = {
                                    navController.navigate("main_layout") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                },
                                onNavigateToLogin = {
                                    navController.navigate("login") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 2. GİRİŞ & KAYIT
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = {
                                    navController.navigate("main_layout") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onNavigateToSignup = { navController.navigate("signup") }
                            )
                        }

                        composable("signup") {
                            SignupScreen(
                                onSignupSuccess = {
                                    navController.navigate("main_layout") {
                                        popUpTo("signup") { inclusive = true }
                                    }
                                },
                                onNavigateToLogin = { navController.popBackStack() }
                            )
                        }

                        // 3. ANA UYGULAMA (Menülü Ekran)
                        composable("main_layout") {
                            MainLayout(
                                onLogout = {
                                    // Çıkış yapınca Login'e at
                                    navController.navigate("login") {
                                        popUpTo("main_layout") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("search") {
                            SearchScreen(navController = navController)
                        }

                        composable(
                            "chat/{username}?postId={postId}",
                            // Argümanları tanımla (postId opsiyonel)
                            arguments = listOf(
                                androidx.navigation.navArgument("username") { type = androidx.navigation.NavType.StringType },
                                androidx.navigation.navArgument("postId") {
                                    type = androidx.navigation.NavType.LongType
                                    defaultValue = 0L
                                }
                            )
                        ) { backStackEntry ->
                            val username = backStackEntry.arguments?.getString("username") ?: ""
                            val postId = backStackEntry.arguments?.getLong("postId")
                            ChatScreen(receiverUsername = username, sharedPostId = postId)
                        }

                        composable("add_story") {
                            AddStoryScreen(onStoryAdded = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}