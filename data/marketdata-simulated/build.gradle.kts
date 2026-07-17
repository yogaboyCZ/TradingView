plugins {
    id("tradingview.android-library")
    alias(libs.plugins.koin.compiler)
}

android { namespace = "cz.yogaboy.data.marketdata.simulated" }

dependencies {
    implementation(project(":domain:marketdata"))
    implementation(libs.koin.android)
    implementation(libs.koin.annotations)
}
