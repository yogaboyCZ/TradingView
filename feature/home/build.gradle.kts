plugins {
    id("tradingview.android-library")
    id("tradingview.compose-conventions")
}

android { namespace = "cz.yogaboy.feature.home" }

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:design"))
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)
    implementation(libs.koin.core)
}