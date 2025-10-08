import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.freeCompilerArgs.add("-Xcontext-parameters")
}

plugins {
    alias(libs.plugins.android.library)
    id("tradingview.android-library")
}

android {
    namespace = "cz.yogaboy.core.common"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.koin.compose.viewmodel)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
}