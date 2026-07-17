package cz.yogaboy.data.marketdata.cache

object CachePolicy {
    const val PRICE_FRESH = 15L * 60L * 1_000L
    const val PRICE_MAX_STALE = 24L * 60L * 60L * 1_000L
    const val HISTORY_FRESH = 6L * 60L * 60L * 1_000L
    const val PROFILE_FRESH = 7L * 24L * 60L * 60L * 1_000L
    const val NEWS_FRESH = 60L * 60L * 1_000L
    const val DETAILS_MAX_STALE = 7L * 24L * 60L * 60L * 1_000L
    const val PROFILE_MAX_STALE = 30L * 24L * 60L * 60L * 1_000L
}
