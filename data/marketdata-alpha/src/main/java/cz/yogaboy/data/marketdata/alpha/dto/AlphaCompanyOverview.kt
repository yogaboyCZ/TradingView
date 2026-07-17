package cz.yogaboy.data.marketdata.alpha.dto

import com.squareup.moshi.Json

data class AlphaCompanyOverview(
    @param:Json(name = "Symbol") val symbol: String?,
    @param:Json(name = "Name") val name: String?,
    @param:Json(name = "Description") val description: String?,
    @param:Json(name = "Exchange") val exchange: String?,
    @param:Json(name = "Currency") val currency: String?,
    @param:Json(name = "Country") val country: String?,
    @param:Json(name = "Sector") val sector: String?,
    @param:Json(name = "Industry") val industry: String?,
    @param:Json(name = "Address") val address: String?,
)
