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


    implementation ("com.google.firebase:firebase-ml-vision:24.0.3")
    implementation ("com.google.android.gms:play-services-mlkit-object-detection:17.0.0")
}
