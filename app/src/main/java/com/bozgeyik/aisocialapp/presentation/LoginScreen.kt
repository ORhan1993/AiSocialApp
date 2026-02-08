package com.bozgeyik.aisocialapp.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

@Composable
fun LoginScreen() {
    // Durum (State) değişkenleri: Kullanıcının yazdığı metinleri burada tutuyoruz
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize() // Tüm ekranı kapla
            .padding(24.dp), // Kenarlardan boşluk bırak
        verticalArrangement = Arrangement.Center, // Dikeyde ortala
        horizontalAlignment = Alignment.CenterHorizontally // Yatayda ortala
    ) {

        // --- 1. Başlık / Logo Kısmı ---
        Text(
            text = "AI Social",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        // --- 2. E-posta Alanı ---
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-posta") },
            leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- 3. Şifre Alanı ---
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Şifre") },
            leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                // Göz ikonuna basınca şifreyi göster/gizle
                val image = if (passwordVisible)
                    Icons.Filled.Visibility
                else
                    Icons.Filled.VisibilityOff

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = "Şifreyi Göster")
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- 4. Giriş Butonu ---
        Button(
            onClick = {
                // Şimdilik sadece konsola yazdıralım
                println("Giriş deneniyor: $email")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = "Giriş Yap", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 5. Kayıt Ol Linki ---
        TextButton(onClick = { /* Kayıt ekranına git */ }) {
            Text("Hesabın yok mu? Kayıt Ol")
        }
    }
}