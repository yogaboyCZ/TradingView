package cz.yogaboy.feature.home.domain

data class Symbol(val symbol: String, val name: String, val region: String, val currency: String)

fun interface SearchSymbolsUseCase {
    suspend operator fun invoke(query: String): List<Symbol>
}

data class SymbolUi(val symbol: String, val name: String, val subtitle: String)

fun Symbol.toUi(): SymbolUi =
    SymbolUi(symbol = symbol, name = name, subtitle = listOf(region, currency).filter { it.isNotBlank() }.joinToString(" • "))
