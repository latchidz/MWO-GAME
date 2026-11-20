plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.latchi.mwo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.latchi.mwo"
        minSdk = 21
        targetSdk = 34
        versionCode = (System.getenv("BUILD_VERSION_CODE")?.toIntOrNull() ?: 2026083102)
        versionName = (System.getenv("BUILD_VERSION_NAME") ?: "1.0.1")
    }

    signingConfigs {
        create("release") {
            storeFile = file("${rootProject.projectDir}/latchi.jks")
            storePassword = "latchi2026"
            keyAlias = "latchi"
            keyPassword = "latchi2026"
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            isShrinkResources = false
        }
        debug {
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions { jvmTarget = "1.8" }
}

dependencies {
    implementation("androidx.browser:browser:1.8.0")
}
