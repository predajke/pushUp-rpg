import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

// Read keystore credentials from local.properties (gitignored).
val keystoreProperties = Properties()
val keystorePropertiesFile = rootProject.file("local.properties")
if (keystorePropertiesFile.exists()) {
    keystorePropertiesFile.inputStream().use { keystoreProperties.load(it) }
}

android {
    namespace = "com.ninthbalcony.pushuprpg"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ninthbalcony.pushuprpg"
        minSdk = 29
        targetSdk = 35
        versionCode = 6
        versionName = "0.9.7"

        // Bundle native debug symbols into the AAB so Play Console / Crashlytics
        // can de-symbolicate native crash stack traces (Firebase, Play Services, Lottie).
        ndk {
            debugSymbolLevel = "FULL"
        }
    }

    signingConfigs {
        create("release") {
            val ksFile = keystoreProperties.getProperty("KEYSTORE_FILE")
            if (!ksFile.isNullOrBlank() && file(ksFile).exists()) {
                storeFile = file(ksFile)
                storePassword = keystoreProperties.getProperty("KEYSTORE_PASSWORD")
                keyAlias = keystoreProperties.getProperty("KEY_ALIAS")
                keyPassword = keystoreProperties.getProperty("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Use release signing config only when local.properties has keystore info.
            // Otherwise produce app-release-unsigned.apk (CI / forks without keystore).
            if (keystoreProperties.getProperty("KEYSTORE_FILE").orEmpty().isNotBlank()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Core Android & Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.viewmodel.compose)


    // Database & Storage
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.datastore.preferences)

    // Background Work & Sync
    implementation(libs.androidx.work.runtime.ktx)

    // Serialization
    implementation(libs.gson)

    // Google Services
    implementation(libs.play.services.auth)
    implementation("com.google.android.gms:play-services-ads:22.6.0")
    implementation("com.google.android.gms:play-services-games-v2:20.1.2")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    // Bridges Firebase's Task<T> → Kotlin coroutines (.await())
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

    // Network Connectivity
    implementation("androidx.work:work-runtime-ktx:2.10.1")

    // Testing
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)

    // Lottie animations
    implementation("com.airbnb.android:lottie-compose:6.4.0")

    // Image loading (Google avatar URLs from Play Games SDK)
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Debug
    debugImplementation(libs.androidx.ui.tooling)
}