plugins {
    id("tradingview.android-library")
    id("tradingview.compose-conventions")
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "cz.yogaboy.core.design"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
}