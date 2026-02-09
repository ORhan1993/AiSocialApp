// Dosya: app/build.gradle.kts
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0"
}

android {
    namespace = "com.bozgeyik.aisocialapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.bozgeyik.aisocialapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        // Bu sürüm Kotlin 1.9.22 ile tam uyumludur
        kotlinCompilerExtensionVersion = "1.5.10"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Standart Android Kütüphaneleri
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // --- SUPABASE (Firebase Yerine) ---
    // Supabase'in en güncel ve stabil Kotlin SDK'sı
    implementation(platform("io.github.jan-tennert.supabase:bom:2.5.0"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt") // Veritabanı
    implementation("io.github.jan-tennert.supabase:storage-kt")   // Resim Yükleme
    implementation("io.github.jan-tennert.supabase:gotrue-kt")    // Giriş/Kayıt (Auth)
    // Supabase için gerekli internet motoru (Ktor)
    implementation("io.ktor:ktor-client-cio:2.3.12")

    // Coil (Resim Gösterme)
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Navigasyon
    implementation("androidx.navigation:navigation-compose:2.7.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.compose.material:material-icons-extended:1.6.0")
}