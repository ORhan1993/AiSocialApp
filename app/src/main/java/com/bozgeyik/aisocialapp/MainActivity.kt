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

// Mevcut importların
import com.bozgeyik.aisocialapp.ui.screens.AddStoryScreen
import com.bozgeyik.aisocialapp.ui.screens.ChatScreen
import com.bozgeyik.aisocialapp.presentation.LoginScreen
import com.bozgeyik.aisocialapp.ui.screens.MainLayout
import com.bozgeyik.aisocialapp.ui.screens.SearchScreen
import com.bozgeyik.aisocialapp.ui.screens.SignupScreen
import com.bozgeyik.aisocialapp.ui.screens.SplashScreen
import com.bozgeyik.aisocialapp.ui.theme.AiSocialAppTheme

// --- YENİ EKLENEN IMPORTLAR (Şifre Sıfırlama İçin) ---
import com.bozgeyik.aisocialapp.data.SupabaseClient
import io.github.jan.supabase.gotrue.handleDeeplinks
import com.bozgeyik.aisocialapp.ui.screens.UpdatePasswordScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Supabase'in gelen linki yakalayıp arka planda oturum açmasını sağla
        SupabaseClient.client.handleDeeplinks(intent)

        // 2. Gelen link bir şifre sıfırlama talebi mi kontrol et
        var startScreen = "splash"
        val uri = intent?.data
        if (uri != null && uri.scheme == "aisocial" && uri.fragment?.contains("type=recovery") == true) {
            startScreen = "update_password" // Eğer şifre sıfırlama linkiyse bu ekranla başla
        }

        setContent {
            AiSocialAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // Navigasyon Yapısı (startDestination değişkenini kullanıyoruz)
                    NavHost(navController = navController, startDestination = startScreen) {

                        // --- YENİ EKLENEN EKRAN (ŞİFRE GÜNCELLEME) ---
                        composable("update_password") {
                            UpdatePasswordScreen(
                                onPasswordUpdated = {
                                    // Şifre güncellendikten sonra Login ekranına yönlendir
                                    navController.navigate("login") {
                                        popUpTo("update_password") { inclusive = true }
                                    }
                                }
                            )
                        }

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