package cz.yogaboy.data.marketdata.twelvedata.repository

import cz.yogaboy.domain.marketdata.CompanyDetailsRepository
import cz.yogaboy.domain.marketdata.CompanyNews
import cz.yogaboy.domain.marketdata.CompanyProfile
import cz.yogaboy.domain.marketdata.CompanyProfileRepository
import cz.yogaboy.domain.marketdata.CompanyProfileSource
import cz.yogaboy.domain.marketdata.PricePoint
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FallbackCompanyDetailsRepositoryTest {
    @Test
    fun `uses Twelve Data profile when it is available`() = runTest {
        val primary = FakeDetailsRepository(profile(source = CompanyProfileSource.TWELVE_DATA))
        val fallback = RecordingProfileRepository(profile(source = CompanyProfileSource.ALPHA_VANTAGE))
        val repository = FallbackCompanyDetailsRepository(primary, fallback)

        val result = repository.getCompanyProfile("AAPL")

        assertEquals(CompanyProfileSource.TWELVE_DATA, result.source)
        assertEquals(0, fallback.callCount)
    }

    @Test
    fun `uses Alpha Vantage profile when Twelve Data fails`() = runTest {
        val primary = FakeDetailsRepository(error = IllegalStateException("TD unavailable"))
        val fallback = RecordingProfileRepository(profile(source = CompanyProfileSource.ALPHA_VANTAGE))
        val repository = FallbackCompanyDetailsRepository(primary, fallback)

        val result = repository.getCompanyProfile("AAPL")

        assertEquals(CompanyProfileSource.ALPHA_VANTAGE, result.source)
        assertEquals(1, fallback.callCount)
    }

    private fun profile(source: CompanyProfileSource) = CompanyProfile(
        name = "Apple",
        exchange = "NASDAQ",
        micCode = null,
        sector = "Technology",
        industry = null,
        employees = null,
        website = null,
        description = "Description",
        chiefExecutiveOfficer = null,
        address = null,
        city = null,
        state = null,
        country = "USA",
        phone = null,
        source = source,
    )

    private class FakeDetailsRepository(
        private val profile: CompanyProfile? = null,
        private val error: Throwable? = null,
    ) : CompanyDetailsRepository {
        override suspend fun getCompanyProfile(ticker: String): CompanyProfile =
            error?.let { throw it } ?: checkNotNull(profile)

        override suspend fun getDailyHistory(ticker: String): List<PricePoint> = emptyList()
        override suspend fun getCompanyNews(ticker: String): List<CompanyNews> = emptyList()
    }

    private class RecordingProfileRepository(
        private val profile: CompanyProfile,
    ) : CompanyProfileRepository {
        var callCount = 0
            private set

        override suspend fun getCompanyProfile(ticker: String): CompanyProfile {
            callCount++
            return profile
        }
    }
}
