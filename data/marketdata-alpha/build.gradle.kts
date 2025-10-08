import java.util.Properties

plugins {
    id("tradingview.android-library")
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

    val apiKey = resolveApiKey("API_KEY_ALPHA")
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
    buildFeatures { buildConfig = true }
}

dependencies {
    implementation(project(":core:network"))
    implementation(project(":domain:marketdata"))
    implementation(libs.squareup.retrofit2)
    implementation(libs.squareup.retrofit2.converter.moshi)
    implementation(libs.squareup.moshi.kotlin)
    implementation(libs.koin.core)
}