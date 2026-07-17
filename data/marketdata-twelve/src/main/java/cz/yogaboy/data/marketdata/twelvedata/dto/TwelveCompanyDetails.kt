package cz.yogaboy.data.marketdata.twelvedata.dto

import com.squareup.moshi.Json

data class TwelveTimeSeries(
    val values: List<TwelveTimeSeriesValue>?,
    val status: String?,
)

data class TwelveTimeSeriesValue(
    val datetime: String?,
    val open: String?,
    val high: String?,
    val low: String?,
    val close: String?,
    val volume: String?,
)

data class TwelveCompanyProfile(
    val name: String?,
    val exchange: String?,
    @param:Json(name = "mic_code") val micCode: String?,
    val sector: String?,
    val industry: String?,
    val employees: Int?,
    val website: String?,
    val description: String?,
    @param:Json(name = "CEO") val chiefExecutiveOfficer: String?,
    val address: String?,
    val city: String?,
    val state: String?,
    val country: String?,
    val phone: String?,
)

data class TwelvePressReleases(
    @param:Json(name = "press_releases") val pressReleases: List<TwelvePressRelease>?,
    val status: String?,
)

data class TwelvePressRelease(
    val id: String?,
    val datetime: String?,
    val title: String?,
    val body: String?,
)
