package cz.yogaboy.core.network

object NetworkConfig {
    val apiKey: String get() = BuildConfig.API_KEY
    val baseUrl: String get() = BuildConfig.BASE_URI
}
