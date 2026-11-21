plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.latchi.mwo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.latchi.mwo"
        minSdk = 21
        targetSdk = 34
        versionCode = (System.getenv("BUILD_VERSION_CODE")?.toIntOrNull() ?: 2026083103)
        versionName = (System.getenv("BUILD_VERSION_NAME") ?: "1.1.0")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    // ARM64 GeckoView browser engine (Firefox), embedded in the app.
    implementation("org.mozilla.geckoview:geckoview-arm64-v8a:150.0.20260511200624")
}
