plugins {
    alias(libs.plugins.android.application)
    id ("com.google.gms.google-services") // Plugin cho Firebase
}

android {
    namespace = "com.example.nhandienvatthe"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.nhandienvatthe"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation ("androidx.camera:camera-core:1.2.0")
    implementation ("androidx.camera:camera-camera2:1.2.0")
    implementation ("androidx.camera:camera-lifecycle:1.2.0")
    implementation ("androidx.camera:camera-viewfinder-compose:1.0.0-alpha02")
    implementation ("androidx.camera:camera-view:1.4.0-beta01")
    // Cập nhật Firebase và ML Kit (Firebase ML Vision có thể không còn cần thiết)
    implementation ("com.google.mlkit:object-detection:17.0.2")
    implementation ("com.google.mlkit:vision-common:17.3.0")
    implementation ("com.google.firebase:firebase-database:20.1.0")
}
