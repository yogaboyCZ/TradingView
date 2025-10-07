includeBuild("build-logic")

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.application") version "8.13.0"
        id("com.android.library") version "8.13.0"
        id("org.jetbrains.kotlin.plugin.compose") version "2.2.20"
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "TradingView"
include(":app")
include(":core:common")
include(":core:design")
include(":core:network")
include(":domain:marketdata")
include(":data:marketdata-alpha")
include(":feature:home")
include(":feature:stocks")

include(":data:marketdata-twelve")
