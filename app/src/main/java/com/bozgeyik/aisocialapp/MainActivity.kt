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
import com.bozgeyik.aisocialapp.presentation.LoginScreen
import com.bozgeyik.aisocialapp.presentation.SignupScreen
import com.bozgeyik.aisocialapp.ui.screens.MainLayout
import com.bozgeyik.aisocialapp.ui.screens.SplashScreen
import com.bozgeyik.aisocialapp.ui.theme.AiSocialAppTheme
import com.bozgeyik.aisocialapp.ui.theme.ThemeViewModel

class MainActivity : ComponentActivity() {

    // EKLENDİ: Global Tema ViewModel'ini oluştur
    private lateinit var themeViewModel: ThemeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // EKLENDİ: ViewModel'i activity context'i ile başlat
        themeViewModel = ThemeViewModel(this)

        setContent {
            // GÜNCELLENDİ: Temayı ViewModel'deki anlık tercihi dinleyecek şekilde uygula
            AiSocialAppTheme(themePreference = themeViewModel.currentThemePreference) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Kök (Root) Navigasyon Kontrolcüsü (Giriş yapılıp yapılmadığını yönetir)
                    val rootNavController = rememberNavController()

                    NavHost(navController = rootNavController, startDestination = "splash") {

                        // 1. SPLASH (AÇILIŞ) EKRANI
                        composable("splash") {
                            SplashScreen(
                                onNavigateToHome = {
                                    rootNavController.navigate("main") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                },
                                onNavigateToLogin = {
                                    rootNavController.navigate("login") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 2. GİRİŞ YAP EKRANI
                        composable("login") {
                            LoginScreen(
                                onNavigateToSignup = { rootNavController.navigate("signup") },
                                onLoginSuccess = {
                                    rootNavController.navigate("main") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 3. KAYIT OL EKRANI
                        composable("signup") {
                            SignupScreen(
                                onNavigateToLogin = { rootNavController.popBackStack() },
                                onSignupSuccess = {
                                    rootNavController.navigate("main") {
                                        popUpTo("signup") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 4. ANA UYGULAMA İSKELETİ (MAIN LAYOUT)
                        composable("main") {
                            MainLayout(
                                themeViewModel = themeViewModel, // Tema bilgisini ayarlara ulaştırmak için gönderiyoruz
                                onLogout = {
                                    rootNavController.navigate("login") {
                                        popUpTo("main") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}