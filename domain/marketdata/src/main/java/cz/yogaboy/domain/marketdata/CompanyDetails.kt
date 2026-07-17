package cz.yogaboy.domain.marketdata

data class PricePoint(
    val date: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long?,
)

data class CompanyProfile(
    val name: String,
    val exchange: String?,
    val micCode: String?,
    val sector: String?,
    val industry: String?,
    val employees: Int?,
    val website: String?,
    val description: String?,
    val chiefExecutiveOfficer: String?,
    val address: String?,
    val city: String?,
    val state: String?,
    val country: String?,
    val phone: String?,
)

data class CompanyNews(
    val id: String,
    val date: String?,
    val title: String,
    val summary: String?,
)

interface CompanyDetailsRepository {
    suspend fun getDailyHistory(ticker: String): List<PricePoint>
    suspend fun getCompanyProfile(ticker: String): CompanyProfile
    suspend fun getCompanyNews(ticker: String): List<CompanyNews>
}
