package cz.yogaboy.data.marketdata.alpha.repository

import cz.yogaboy.data.marketdata.alpha.network.AlphaVantageApi
import cz.yogaboy.domain.marketdata.CompanyProfile
import cz.yogaboy.domain.marketdata.CompanyProfileRepository
import cz.yogaboy.domain.marketdata.CompanyProfileSource

class AlphaCompanyProfileRepository(
    private val api: AlphaVantageApi,
    private val apiKey: String,
) : CompanyProfileRepository {
    override suspend fun getCompanyProfile(ticker: String): CompanyProfile {
        require(':' !in ticker) { "Alpha Vantage fallback zatím podporuje US tickery." }
        val overview = api.getCompanyOverview(symbol = ticker, apiKey = apiKey)
        val name = overview.name?.takeIf(String::isNotBlank)
            ?: error("Alpha Vantage neposkytla profil společnosti.")

        return CompanyProfile(
            name = name,
            exchange = overview.exchange,
            micCode = null,
            sector = overview.sector,
            industry = overview.industry,
            employees = null,
            website = null,
            description = overview.description,
            chiefExecutiveOfficer = null,
            address = overview.address,
            city = null,
            state = null,
            country = overview.country,
            phone = null,
            source = CompanyProfileSource.ALPHA_VANTAGE,
        )
    }
}
