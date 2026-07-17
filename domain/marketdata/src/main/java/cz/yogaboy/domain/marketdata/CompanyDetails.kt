package cz.yogaboy.domain.marketdata

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

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
    val source: CompanyProfileSource = CompanyProfileSource.UNKNOWN,
)

enum class CompanyProfileSource(val badge: String) {
    TWELVE_DATA("TD"),
    ALPHA_VANTAGE("AV"),
    UNKNOWN("—"),
}

interface CompanyProfileRepository {
    suspend fun getCompanyProfile(ticker: String): CompanyProfile
}

data class CompanyNews(
    val id: String,
    val date: String?,
    val title: String,
    val summary: String?,
)

interface CompanyDetailsRepository : CompanyProfileRepository {
    suspend fun getDailyHistory(ticker: String): List<PricePoint>
    fun observeDailyHistory(ticker: String): Flow<List<PricePoint>?> = flowOf(null)
    suspend fun getCompanyNews(ticker: String): List<CompanyNews>
}
