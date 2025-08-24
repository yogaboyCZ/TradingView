package cz.yogaboy.feature.home.presentation

sealed interface HomeEvent {
    data class QueryChanged(val value: String) : HomeEvent
    data object Submit : HomeEvent
    data class Select(val symbol: String) : HomeEvent
}
