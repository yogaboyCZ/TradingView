plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation("com.android.tools.build:gradle:8.13.0")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.20")
}

gradlePlugin {
    plugins {
        create("tradingviewAndroidLibrary") {
            id = "tradingview.android-library"
            implementationClass = "TradingviewAndroidLibraryPlugin"
        }
        create("tradingviewComposeConventions") {
            id = "tradingview.compose-conventions"
            implementationClass = "TradingviewComposeConventionsPlugin"
        }
    }
}