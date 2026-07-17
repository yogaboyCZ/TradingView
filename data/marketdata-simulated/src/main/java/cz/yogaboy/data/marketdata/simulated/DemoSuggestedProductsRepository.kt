package cz.yogaboy.data.marketdata.simulated

import cz.yogaboy.domain.marketdata.ProductCategory
import cz.yogaboy.domain.marketdata.SuggestedProduct
import cz.yogaboy.domain.marketdata.SuggestedProductsRepository

class DemoSuggestedProductsRepository : SuggestedProductsRepository {
    override fun getSuggestedProducts(): List<SuggestedProduct> = products

    private companion object {
        val products = listOf(
            SuggestedProduct("AAPL", "Apple", ProductCategory.TECHNOLOGY, 334.20, 2.04, showTrendSummary = true),
            SuggestedProduct("MSFT", "Microsoft", ProductCategory.TECHNOLOGY, 403.44, 1.97),
            SuggestedProduct("NVDA", "NVIDIA", ProductCategory.SEMICONDUCTORS, 206.97, -2.60, showTrendSummary = true),
            SuggestedProduct("GOOGL", "Alphabet", ProductCategory.INTERNET, 355.55, -4.14),
            SuggestedProduct("AMZN", "Amazon", ProductCategory.E_COMMERCE, 252.49, -0.97),
            SuggestedProduct("KO", "Coca-Cola", ProductCategory.CONSUMER_GOODS, 69.18, 0.42, showTrendSummary = true),
            SuggestedProduct("BRK.B", "Berkshire Hathaway", ProductCategory.FINANCE, 503.71, 0.81),
            SuggestedProduct("BA", "Boeing", ProductCategory.AEROSPACE, 224.86, -1.35, showTrendSummary = true),
            SuggestedProduct("TSLA", "Tesla", ProductCategory.AUTOMOTIVE, 390.13, -1.10),
            SuggestedProduct("JPM", "JPMorgan Chase", ProductCategory.BANKING, 289.42, 0.64, showTrendSummary = true),
            SuggestedProduct("SAP:XETR", "SAP", ProductCategory.ENTERPRISE_SOFTWARE, 250.0, 0.0, "EUR"),
            SuggestedProduct("ASML:XAMS", "ASML", ProductCategory.SEMICONDUCTOR_EQUIPMENT, 1_000.0, 0.0, "EUR"),
            SuggestedProduct("CEZ:XPRA", "ČEZ", ProductCategory.ENERGY, 1_200.0, 0.0, "CZK"),
        )
    }
}
