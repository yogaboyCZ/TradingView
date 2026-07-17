plugins {
    id("tradingview.android-library")
    alias(libs.plugins.com.google.devtools.ksp)
    alias(libs.plugins.koin.compiler)
}

android {
    namespace = "cz.yogaboy.data.marketdata.cache"
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

dependencies {
    implementation(project(":domain:marketdata"))
    implementation(libs.androidx.room3.runtime)
    ksp(libs.androidx.room3.compiler)
    implementation(libs.squareup.moshi.kotlin)
    implementation(libs.koin.android)
    implementation(libs.koin.annotations)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kotlinx.coroutines.test)
    testRuntimeOnly(libs.junit.platform.launcher)
}
