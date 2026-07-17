package cz.yogaboy.data.marketdata.twelvedata.repository

import cz.yogaboy.data.marketdata.twelvedata.network.TwelveDataApi
import cz.yogaboy.domain.marketdata.CompanyDetailsRepository
import cz.yogaboy.domain.marketdata.CompanyNews
import cz.yogaboy.domain.marketdata.CompanyProfile
import cz.yogaboy.domain.marketdata.PricePoint

class TwelveCompanyDetailsRepository(
    private val api: TwelveDataApi,
    private val apiKey: String,
) : CompanyDetailsRepository {

    override suspend fun getDailyHistory(ticker: String): List<PricePoint> =
        api.getTimeSeries(symbol = ticker, apiKey = apiKey).values.orEmpty().mapNotNull { value ->
            PricePoint(
                date = value.datetime ?: return@mapNotNull null,
                open = value.open?.toDoubleOrNull() ?: return@mapNotNull null,
                high = value.high?.toDoubleOrNull() ?: return@mapNotNull null,
                low = value.low?.toDoubleOrNull() ?: return@mapNotNull null,
                close = value.close?.toDoubleOrNull() ?: return@mapNotNull null,
                volume = value.volume?.toDoubleOrNull()?.toLong(),
            )
        }.also { require(it.isNotEmpty()) { "Twelve Data neposkytla historii ceny." } }

    override suspend fun getCompanyProfile(ticker: String): CompanyProfile =
        api.getProfile(symbol = ticker, apiKey = apiKey).let { profile ->
            CompanyProfile(
                name = profile.name ?: ticker,
                exchange = profile.exchange,
                micCode = profile.micCode,
                sector = profile.sector,
                industry = profile.industry,
                employees = profile.employees,
                website = profile.website,
                description = profile.description,
                chiefExecutiveOfficer = profile.chiefExecutiveOfficer,
                address = profile.address,
                city = profile.city,
                state = profile.state,
                country = profile.country,
                phone = profile.phone,
            )
        }

    override suspend fun getCompanyNews(ticker: String): List<CompanyNews> =
        api.getPressReleases(symbol = ticker, apiKey = apiKey).pressReleases.orEmpty().mapNotNull {
            CompanyNews(
                id = it.id ?: return@mapNotNull null,
                date = it.datetime,
                title = it.title ?: return@mapNotNull null,
                summary = it.body
                    ?.replace(Regex("<[^>]*>"), " ")
                    ?.replace(Regex("\\s+"), " ")
                    ?.trim()
                    ?.take(240),
            )
        }
}
