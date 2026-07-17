plugins {
    id("tradingview.android-library")
    id("tradingview.compose-conventions")
    alias(libs.plugins.koin.compiler)
}

android { namespace = "cz.yogaboy.feature.home" }

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:design"))
    implementation(project(":domain:marketdata"))
    implementation(libs.koin.annotations)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)
    implementation(libs.koin.core)
}
