package cz.yogaboy.domain.marketdata

data class SuggestedProduct(
    val ticker: String,
    val companyName: String,
    val category: ProductCategory,
    val fallbackPrice: Double,
    val fallbackDailyChange: Double,
    val currency: String = "USD",
    val showTrendSummary: Boolean = false,
)

enum class ProductCategory {
    TECHNOLOGY,
    SEMICONDUCTORS,
    INTERNET,
    E_COMMERCE,
    CONSUMER_GOODS,
    FINANCE,
    AEROSPACE,
    AUTOMOTIVE,
    BANKING,
    ENTERPRISE_SOFTWARE,
    SEMICONDUCTOR_EQUIPMENT,
    ENERGY,
}

interface SuggestedProductsRepository {
    fun getSuggestedProducts(): List<SuggestedProduct>
}
