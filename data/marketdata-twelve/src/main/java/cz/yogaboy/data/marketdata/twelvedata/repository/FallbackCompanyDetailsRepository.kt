package cz.yogaboy.data.marketdata.twelvedata.repository

import cz.yogaboy.domain.marketdata.CompanyDetailsRepository
import cz.yogaboy.domain.marketdata.CompanyNews
import cz.yogaboy.domain.marketdata.CompanyProfile
import cz.yogaboy.domain.marketdata.CompanyProfileRepository
import cz.yogaboy.domain.marketdata.PricePoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

class FallbackCompanyDetailsRepository(
    private val primary: CompanyDetailsRepository,
    private val profileFallback: CompanyProfileRepository,
) : CompanyDetailsRepository {
    override suspend fun getDailyHistory(ticker: String): List<PricePoint> =
        primary.getDailyHistory(ticker)

    override fun observeDailyHistory(ticker: String): Flow<List<PricePoint>?> =
        primary.observeDailyHistory(ticker)

    override suspend fun getCompanyProfile(ticker: String): CompanyProfile =
        try {
            primary.getCompanyProfile(ticker)
        } catch (primaryError: Throwable) {
            if (primaryError is CancellationException) throw primaryError
            try {
                profileFallback.getCompanyProfile(ticker)
            } catch (fallbackError: Throwable) {
                if (fallbackError is CancellationException) throw fallbackError
                fallbackError.addSuppressed(primaryError)
                throw fallbackError
            }
        }

    override suspend fun getCompanyNews(ticker: String): List<CompanyNews> =
        primary.getCompanyNews(ticker)
}
