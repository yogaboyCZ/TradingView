import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
//    alias(libs.plugins.com.google.devtools.ksp)
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
    namespace = "cz.yogaboy.core.network"
    compileSdk = 36

    defaultConfig {
        minSdk = 33
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    val apiKey = resolveApiKey("API_KEY")
    if (apiKey.isBlank()) { throw GradleException(
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
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isMinifyEnabled = false
            enableUnitTestCoverage = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures { buildConfig = true }
}

dependencies {

    api(libs.squareup.okhttp3)
    api(libs.squareup.okhttp3.logging.interceptor)

    api(libs.squareup.retrofit2)
    api(libs.squareup.retrofit2.converter.moshi)
//    api(libs.squareup.moshi.kotlin)
//    ksp(libs.squareup.moshi.kotlin.codegen)

//    implementation(libs.koin.android)
    implementation(libs.koin.core)
    debugImplementation(libs.chucker.log)
    releaseImplementation(libs.chucker.log.no.op)

}
