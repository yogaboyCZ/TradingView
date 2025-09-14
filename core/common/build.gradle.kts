plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "cz.yogaboy.core.common"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 33

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.koin.compose.viewmodel)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
}