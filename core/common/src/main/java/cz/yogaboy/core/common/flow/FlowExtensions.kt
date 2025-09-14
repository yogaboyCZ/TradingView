package cz.yogaboy.core.common.flow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*

context(viewModel: ViewModel)
fun <T> Flow<T>.stateInWhileSubscribed(initial: T): StateFlow<T> =
    stateIn(viewModel.viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)

context(viewModel: ViewModel)
fun <T, R> Flow<T>.mapToState(initial: R, transform: suspend (T) -> R): StateFlow<R> =
    map { transform(it) }.stateInWhileSubscribed(initial)

context(viewModel: ViewModel)
fun <T> Flow<T>.shareWhileSubscribed(): SharedFlow<T> =
    shareIn(viewModel.viewModelScope, SharingStarted.WhileSubscribed(5_000), replay = 0)

fun <T> Flow<T>.windowedBy(timeMs: Long): Flow<List<T>> =
    flow {
        var bucket = mutableListOf<T>()
        var last = 0L
        collect { v ->
            val now = System.nanoTime() / 1_000_000
            if (last == 0L) last = now
            if (now - last >= timeMs) {
                emit(bucket)
                bucket = mutableListOf()
                last = now
            }
            bucket.add(v)
        }
    }

data class Resource<out T>(val data: T?, val loading: Boolean, val error: Throwable?)
fun <T> loading(data: T? = null) = Resource(data, true, null)
fun <T> success(data: T) = Resource(data, false, null)
fun <T> failure(err: Throwable, data: T? = null) = Resource(data, false, err)