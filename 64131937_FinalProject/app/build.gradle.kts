plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
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
    viewBinding {
        enable = true
    }
}

dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.mlkit:object-detection:17.0.2")
    implementation("com.google.mlkit:vision-common:17.3.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.activity:activity-ktx:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-database-ktx:20.3.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("androidx.camera:camera-core:1.2.0")
    implementation("androidx.camera:camera-camera2:1.2.0")
    implementation("androidx.camera:camera-lifecycle:1.2.0")
    implementation("androidx.camera:camera-viewfinder-compose:1.0.0-alpha02")
    implementation("androidx.camera:camera-view:1.4.0-beta01")
    implementation("com.google.mlkit:object-detection:17.0.2")
    implementation("com.google.mlkit:vision-common:17.3.0")
    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation("androidx.camera:camera-extensions:1.4.0")
    implementation("com.google.firebase:firebase-storage:20.1.0")
    implementation("com.google.firebase:firebase-auth:21.1.0")
    implementation("com.google.firebase:firebase-appcheck:16.0.0")
    implementation("com.google.firebase:firebase-core:21.1.0")
    implementation(platform("com.google.firebase:firebase-bom:33.6.0"))
    implementation(kotlin("script-runtime"))
}
