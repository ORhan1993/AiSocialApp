// Dosya: build.gradle.kts (Project: AiSocialApp)
buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.1")
    }
}

plugins {
    // Sürüm numaraları artık libs.versions.toml'dan yönetiliyor
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.google.gms.google.services) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
