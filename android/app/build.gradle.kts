plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "ru.yanus171.android.autobrightness"
    compileSdk = 34

    defaultConfig {
        applicationId = "ru.yanus171.android.autobrightness"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        //testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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


    flavorDimensions += "version"
    productFlavors {
        create("free") {
            dimension = "version"
            setProperty("archivesBaseName", "autobrightness")
        }

    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.material)
}