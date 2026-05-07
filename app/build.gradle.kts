plugins {
    // Gunakan alias (Version Catalog) agar sinkron dengan file libs.versions.toml
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)

    // Tambahkan plugin Google Services untuk Firebase di sini
    id("com.google.gms.google-services")
}

android {
    namespace = "com.albrk.shoescare"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.albrk.shoescare"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // FIREBASE
    // Menggunakan Firebase BoM untuk manajemen versi otomatis
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-analytics")

    // UPDATE DI SINI: Gunakan library Realtime Database (Gantikan Firestore)
    implementation("com.google.firebase:firebase-database")

    // ANDROIDX & COMPOSE
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)

    // ROOM (Bisa tetap ada jika ingin pakai Offline-First)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
}