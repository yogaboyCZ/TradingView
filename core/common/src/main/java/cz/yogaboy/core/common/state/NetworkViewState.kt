package cz.yogaboy.core.common.state

sealed class NetworkViewState<T> {
    data class Success<T>(val value: T) : NetworkViewState<T>()
    data class Error<T>(val throwable: Throwable, val uiErrorMessage: String? = null) : NetworkViewState<T>()
    data class Loading<T>(val text: String = "", val description: String = "") : NetworkViewState<T>()
}