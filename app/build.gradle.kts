plugins {
    id("com.android.application")
}

android {
    namespace = "com.android.bounce"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.android.bounce"
        minSdk = 19
        targetSdk = 34
        versionCode = 1111
        versionName = "1.1"
    }

    buildTypes {
        debug {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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
}

dependencies {
    // Game murni native Canvas & Activity tanpa dependensi pihak ketiga
}
