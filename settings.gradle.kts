pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
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