import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.plugin.compose)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    // alias(libs.plugins.google.services) — re-enable when you add your own google-services.json
}

kotlin {
    jvmToolchain(17)
}

// Configure Java toolchain explicitly for Android tasks
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

android {
    namespace = "id.startapp.pheromone"
    compileSdk = 35

    defaultConfig {
        applicationId = "id.startapp.pheromone"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        // Build config fields — override via local.properties: API_BASE_URL=http://YOUR_IP:3000
        buildConfigField("String", "API_BASE_URL", "\"${project.findProperty("API_BASE_URL") ?: "http://10.0.2.2:3000"}\"")
        buildConfigField("String", "GRPC_WEB_URL", "\"${project.findProperty("GRPC_WEB_URL") ?: "http://10.0.2.2:50051"}\"")

        // Google Maps API Key - set in gradle.properties or local.properties
        // Get your API key from: https://console.cloud.google.com/apis/credentials
        val mapsApiKey = project.findProperty("GOOGLE_MAPS_API_KEY") ?: ""
        manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = mapsApiKey
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "API_BASE_URL", "\"${project.findProperty("API_BASE_URL") ?: "https://api.production.com"}\"")
        }
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            // Default: 10.0.2.2 = host machine from Android emulator
            // For physical devices, set API_BASE_URL in local.properties
            buildConfigField("String", "API_BASE_URL", "\"${project.findProperty("API_BASE_URL") ?: "http://10.0.2.2:3000"}\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    // composeOptions not needed with Kotlin 2.0+ and Compose Compiler plugin

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    dependencies {
        // Project dependency
        implementation(project(":shared"))

        // Compose
        implementation(libs.compose.ui)
        implementation(libs.compose.ui.tooling.preview)
        implementation(libs.compose.material3)
        implementation(libs.compose.activity)
        debugImplementation(libs.compose.ui.tooling)

        // AndroidX
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.lifecycle.runtime)
        implementation(libs.androidx.appcompat)

        // Security - EncryptedSharedPreferences for secure token storage
        implementation("androidx.security:security-crypto:1.1.0-alpha06")

        // Firebase — re-enable when you add your own google-services.json
        // implementation(platform(libs.firebase.bom))
        // implementation(libs.firebase.messaging)

        // Koin
        implementation(libs.koin.android)

        // Decompose (navigation)
        implementation(libs.decompose)
        implementation(libs.decompose.compose)

        // Testing
        testImplementation(libs.kotlin.test)
        androidTestImplementation(libs.androidx.test.junit)
        androidTestImplementation(libs.androidx.espresso.core)
    }
}

