import java.util.Properties
import kotlin.apply

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.com.google.devtools.ksp)
}

fun Project.resolveApiKey(keyName: String): String {
    findProperty(keyName)?.toString()?.takeIf { it.isNotBlank() }?.let { return it }
    val localFile = rootProject.file("local.properties")
    if (localFile.exists()) {
        val props = Properties().apply { localFile.inputStream().use { load(it) } }
        props.getProperty(keyName)?.takeIf { it.isNotBlank() }?.let { return it }
    }
    System.getenv(keyName)?.takeIf { it.isNotBlank() }?.let { return it }
    return ""
}

android {
    namespace = "cz.yogaboy.data.marketdata.alpha"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 33
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    val apiKey = resolveApiKey("API_KEY")
    if (apiKey.isBlank()) {
        throw GradleException(
            """
                API key not found!
                Please add API_KEY to your local.properties file 
                or set it as an environment variable before building.
            """.trimIndent()
        )
    }
    val apiKeyValue = "\"$apiKey\""

    flavorDimensions += "env"
    productFlavors {
        create("dev") {
            dimension = "env"
            buildConfigField("String", "API_KEY", apiKeyValue)
            buildConfigField("String", "BASE_URI", "\"https://www.alphavantage.co/\"")
        }
        create("prod") {
            dimension = "env"
            buildConfigField("String", "API_KEY", apiKeyValue)
            buildConfigField("String", "BASE_URI", "\"https://www.alphavantage.co/\"")
        }
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
    buildFeatures { buildConfig = true }
}

dependencies {
    implementation(project(":core:network"))
    implementation(project(":domain:marketdata"))
    implementation(libs.squareup.retrofit2)
    implementation(libs.squareup.retrofit2.converter.moshi)
    implementation(libs.squareup.moshi.kotlin)
    ksp(libs.squareup.moshi.kotlin.codegen)

    implementation(libs.koin.core)
}