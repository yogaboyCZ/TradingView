package cz.yogaboy.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.yogaboy.feature.home.domain.SearchSymbolsUseCase
import cz.yogaboy.feature.home.domain.SymbolUi
import cz.yogaboy.feature.home.domain.toUi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Idle : HomeUiState
    data object Loading : HomeUiState
    data class Success(val items: List<SymbolUi>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

sealed interface HomeEffect {
    data class NavigateToDetail(val symbol: String) : HomeEffect
}

class HomeViewModel(
    private val search: SearchSymbolsUseCase,
    private val io: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val submit = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val _effects = MutableSharedFlow<HomeEffect>()
    val effects: SharedFlow<HomeEffect> = _effects.asSharedFlow()

    val uiState: StateFlow<HomeUiState> =
        merge(
            query.debounce(400).distinctUntilChanged().map { it },
            submit.map { query.value }
        ).flatMapLatest { q ->
            flow {
                if (q.isBlank()) emit(HomeUiState.Idle)
                else {
                    emit(HomeUiState.Loading)
                    val result = search(q).map { it.toUi() }
                    emit(HomeUiState.Success(result))
                }
            }.catch { emit(HomeUiState.Error(it.message.orEmpty())) }
                .flowOn(io)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState.Idle)

    fun handle(event: HomeEvent) {
        when (event) {
            is HomeEvent.QueryChanged -> query.value = event.value
            HomeEvent.Submit -> submit.tryEmit(Unit)
            is HomeEvent.Select -> viewModelScope.launch {  }
        }
    }
}
