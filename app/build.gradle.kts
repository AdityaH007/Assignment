@file:Suppress("DEPRECATION")

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}


android {
    namespace = "com.example.assignemtn"
    compileSdk = 35

    packaging {
        pickFirst ("lib/arm64-v8a/libc++_shared.so")
        pickFirst ("lib/x86_64/libc++_shared.so")
        pickFirst ("lib/armeabi-v7a/libc++_shared.so")
        pickFirst ("lib/x86/libc++_shared.so")
    }

    defaultConfig {
        applicationId = "com.example.assignemtn"
        minSdk = 28
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //libvlc
    implementation("org.videolan.android:libvlc-all:4.0.0-eap19")


    implementation ("com.arthenica:ffmpeg-kit-full:6.0-2")
}