plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.visionstudio.app"
    compileSdk = 35

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    defaultConfig {
        ndk {
            abiFilters += listOf("arm64-v8a")
        }
        applicationId = "com.visionstudio.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 11
        versionName = "2.0"
    }

    signingConfigs {
        create("release") {
            val keyPath = System.getenv("VS_KEYSTORE_PATH")
            if (!keyPath.isNullOrBlank()) {
                storeFile = file(keyPath)
                storePassword = System.getenv("VS_STORE_PASSWORD")
                keyAlias = System.getenv("VS_KEY_ALIAS")
                keyPassword = System.getenv("VS_KEY_PASSWORD")
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
    packaging { resources.excludes += setOf("META-INF/DEPENDENCIES", "META-INF/LICENSE*") }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
    implementation(composeBom)
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.8.3")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("org.opencv:opencv:4.10.0")
    implementation("org.tensorflow:tensorflow-lite:2.17.0")
    val cameraX = "1.4.1"
    implementation("androidx.camera:camera-camera2:$cameraX")
    implementation("androidx.camera:camera-lifecycle:$cameraX")
    implementation("androidx.camera:camera-view:$cameraX")
}
