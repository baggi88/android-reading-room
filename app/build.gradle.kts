import java.util.Properties
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.File

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
var keystorePath: String? = null
var keystorePassword: String? = null
var keyAlias: String? = null
var keyPassword: String? = null

if (localPropertiesFile.exists() && localPropertiesFile.isFile) {
    try {
        localProperties.load(FileInputStream(localPropertiesFile))
        
        println("--> Properties loaded from local.properties:")
        localProperties.forEach { key, value -> println("-->   '$key' = '$value'") }
        println("--> End of loaded properties.")
        
        keystorePath = localProperties.getProperty("READING_ROOM_KEYSTORE_PATH")
        keystorePassword = localProperties.getProperty("READING_ROOM_KEYSTORE_PASSWORD")
        keyAlias = localProperties["READING_ROOM_KEY_ALIAS"] as? String
        keyPassword = localProperties["READING_ROOM_KEY_PASSWORD"] as? String

    } catch (e: Exception) {
        println("Warning: Could not load local.properties file: ${e.message}")
    }
} else {
     println("Warning: local.properties file not found at ${localPropertiesFile.absolutePath}")
}

android {
    namespace = "com.example.readingroom"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.readingroom"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        val googleBooksApiKey = localProperties.getProperty("GOOGLE_BOOKS_API_KEY") ?: ""
         if (googleBooksApiKey.isEmpty()) {
             println("Warning: GOOGLE_BOOKS_API_KEY not found in local.properties. Google Books search might not work.")
         }
        buildConfigField("String", "GOOGLE_BOOKS_API_KEY", "\"${googleBooksApiKey.replace("\"", "\\\"")}\"")
    }

    signingConfigs {
        create("release") {
            println("--> Reading signing config properties from local.properties:")
            println("--> Keystore Path: $keystorePath")
            println("--> Keystore Password Set: ${keystorePassword != null && keystorePassword!!.isNotEmpty()}")
            println("--> Key Alias: $keyAlias")
            println("--> Key Password Set: ${keyPassword != null && keyPassword!!.isNotEmpty()}")

            if (keystorePath != null && keystorePassword != null && keyAlias != null && keyPassword != null) {
                val keystoreFile = File(keystorePath) 
                if (keystoreFile.exists()) {
                    storeFile = keystoreFile
                    storePassword = keystorePassword
                    this.keyAlias = keyAlias
                    this.keyPassword = keyPassword
                } else {
                    throw FileNotFoundException("Keystore file not found at: $keystorePath. Please check the path in local.properties.")
                }
            } else {
                println("Warning: Signing properties (READING_ROOM_...) missing or incomplete in local.properties. Release build will not be signed.")
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
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Core Android
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("androidx.activity:activity-compose:1.9.1")

    // Compose
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material3:material3-window-size-class")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.material:material")
    implementation("com.google.android.material:material:1.12.0")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Image loading
    implementation("io.coil-kt:coil-compose:2.6.0")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // DataStore Preferences
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.1.1"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

    // Cloudinary for image uploads
    implementation("com.cloudinary:cloudinary-android:2.4.0")
}