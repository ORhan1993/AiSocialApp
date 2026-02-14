package com.bozgeyik.aisocialapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun MainLayout(onLogout: () -> Unit) {
    val navController = rememberNavController()
    // 0: Home, 1: Search, 2: Add, 3: Chat, 4: Profile
    var selectedItem by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                // 1. ANA SAYFA
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Ana Sayfa") },
                    label = { Text("Akış") },
                    selected = selectedItem == 0,
                    onClick = {
                        selectedItem = 0
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )

                // 2. ARAMA
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = "Ara") },
                    label = { Text("Ara") },
                    selected = selectedItem == 1,
                    onClick = {
                        selectedItem = 1
                        navController.navigate("search")
                    }
                )

                // 3. EKLE (Büyük Buton)
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.AddCircle,
                            contentDescription = "Ekle",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    },
                    label = { },
                    selected = false,
                    onClick = {
                        navController.navigate("add_post")
                    }
                )

                // 4. MESAJLAR (SOHBET LİSTESİ)
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Email, contentDescription = "Mesajlar") },
                    label = { Text("Chat") },
                    selected = selectedItem == 3,
                    onClick = {
                        selectedItem = 3
                        navController.navigate("chat_list")
                    }
                )

                // 5. PROFİL
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profil") },
                    label = { Text("Profil") },
                    selected = selectedItem == 4,
                    onClick = {
                        selectedItem = 4
                        navController.navigate("profile")
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            NavHost(navController = navController, startDestination = "home") {

                // 1. ANA SAYFA
                composable("home") {
                    HomeScreen(
                        onNavigateToAddPost = { navController.navigate("add_post") },
                        onNavigateToAddStory = { navController.navigate("add_story") },
                        onNavigateToMessages = {
                            selectedItem = 3
                            navController.navigate("chat_list")
                        },
                        onNavigateToNotifications = { navController.navigate("notifications") },
                        onNavigateToLogin = onLogout
                    )
                }

                // 2. GÖNDERİ EKLEME
                composable("add_post") {
                    AddPostScreen(onPostAdded = {
                        navController.popBackStack()
                        selectedItem = 0
                    })
                }

                // 3. PROFİL
                composable("profile") {
                    ProfileScreen(
                        onLogout = onLogout,
                        onNavigateToSettings = { navController.navigate("settings") }
                    )
                }

                // 4. HİKAYE EKLEME
                composable("add_story") {
                    AddStoryScreen(onStoryAdded = { navController.popBackStack() })
                }

                // 5. ARAMA
                composable("search") {
                    SearchScreen(navController = navController)
                }

                // 6. SOHBET LİSTESİ (INBOX)
                composable("chat_list") {
                    ChatListScreen(navController = navController)
                }

                // 7. SOHBET DETAY
                composable(
                    "chat/{username}",
                    arguments = listOf(navArgument("username") { type = NavType.StringType })
                ) { backStackEntry ->
                    val username = backStackEntry.arguments?.getString("username") ?: ""
                    ChatScreen(receiverUsername = username)
                }

                // 8. AYARLAR
                composable("settings") {
                    SettingsScreen(navController = navController)
                }

                // 9. BİLDİRİMLER
                composable("notifications") {
                    NotificationsScreen(navController = navController)
                }
            }
        }
    }
}