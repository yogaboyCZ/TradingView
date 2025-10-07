plugins {
    id("com.android.library")
}

android {
    buildFeatures { compose = true }
}

dependencies {
    add("implementation", platform("androidx.compose:compose-bom:2025.01.00"))
    add("implementation", "androidx.compose.ui:ui")
    add("implementation", "androidx.compose.material3:material3")
    add("implementation", "androidx.compose.ui:ui-tooling-preview")
    add("debugImplementation", "androidx.compose.ui:ui-tooling")
}