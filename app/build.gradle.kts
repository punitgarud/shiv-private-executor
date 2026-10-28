plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.shiv.executor"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.shiv.executor"
        minSdk = 26
        targetSdk = 34
        versionCode = 5
        versionName = "5.0-BLACK"

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    // Official Solana Mobile Wallet Adapter Client Library for Seeker Seed Vault & MWA wallets
    implementation("com.solana.mobilewalletadapter:mobile-wallet-adapter-clientlib-ktx:2.0.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
}
