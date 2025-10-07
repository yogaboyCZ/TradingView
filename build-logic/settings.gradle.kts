pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.library") version "8.13.0"
        id("org.jetbrains.kotlin.plugin.compose") version "2.2.20"
    }
}

rootProject.name = "build-logic"