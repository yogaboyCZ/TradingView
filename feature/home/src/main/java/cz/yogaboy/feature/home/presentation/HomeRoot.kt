package cz.yogaboy.feature.home.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeRoute(
    onNavigateToDetail: (String) -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { e ->
            when (e) {
                is HomeEffect.NavigateToDetail -> onNavigateToDetail(e.symbol)
            }
        }
    }

    HomeScreen(
        query = query,
        onQueryChange = {
            query = it
            viewModel.handle(HomeEvent.QueryChanged(it))
        },
        onSearch = { viewModel.handle(HomeEvent.Submit) }
    )
}
