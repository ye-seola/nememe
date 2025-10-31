plugins {
    alias(libs.plugins.android.application)
    id("dev.rikka.tools.refine") version "4.4.0"
}

android {
    namespace = "io.nemeneme"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.nemeneme"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.material)
    compileOnly(project(":hidden-api"))
    implementation("dev.rikka.tools.refine:runtime:4.4.0")
    implementation("androidx.preference:preference-ktx:1.2.0")
}