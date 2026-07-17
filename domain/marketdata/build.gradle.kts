plugins {
    id("tradingview.android-library")
}

android {
    namespace = "cz.yogaboy.domain.marketdata"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}